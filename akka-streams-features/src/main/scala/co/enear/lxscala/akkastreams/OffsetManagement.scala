package co.enear.lxscala.akkastreams

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.{ CommittableMessage, CommittableOffsetBatch }
import akka.kafka.scaladsl.{ Consumer, Producer }
import akka.kafka.{ ConsumerSettings, ProducerSettings, Subscriptions }
import akka.stream.javadsl.RunnableGraph
import akka.stream.{ ActorMaterializer, ClosedShape }
import akka.stream.scaladsl.{ Broadcast, Flow, GraphDSL, Sink }
import co.enear.lxscala.twitter.entities.Tweet
import co.enear.lxscala.twitter.util.OffsetDB
import co.enear.lxscala.twitter.util.TweetUtils.parseTweet
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.{ StringDeserializer, StringSerializer }

object OffsetManagement {
  implicit val system = ActorSystem("System")
  implicit val dispatcher = system.dispatcher

  implicit val materializer = ActorMaterializer()

  val producerSettings = ProducerSettings(system, new StringSerializer, new StringSerializer)
  val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)

  val autoKafkaStorage = Consumer.committableSource(consumerSettings, Subscriptions.topics("topic1"))
    .map { record =>
      (record, parseTweet[CommittableMessage[String, String]](record, _.record.value()))
    }.map {
      case (record, tweet) =>
        KafkaUtils.createProducerMessageWithOffset(record.committableOffset, tweet, "topic2")
    }.runWith(Producer.commitableSink(producerSettings))

  val batchedKafkaSource = Consumer.committableSource(consumerSettings, Subscriptions.topics("topic1"))
    .map { record =>
      (record, parseTweet[CommittableMessage[String, String]](record, _.record.value()))
    }

  val tweetSink = Flow[(CommittableMessage[String, String], Tweet)]
    .map { case (_, tweet) => KafkaUtils.createProducerRecord(tweet, "topic2") }
    .to(Producer.plainSink(producerSettings))

  val batchedCommitSink = Flow[(CommittableMessage[String, String], Tweet)]
    .map { case (record, _) => record.committableOffset }
    .batch(max = 20, first => CommittableOffsetBatch.empty.updated(first)) { (batch, elem) =>
      batch.updated(elem)
    }
    .mapAsync(3)(_.commitScaladsl())
    .to(Sink.ignore)

  val graph = GraphDSL.create(tweetSink, batchedCommitSink)((batchedSink, _) => batchedSink) { implicit builder => (batchedSink, twitterSink) =>
    import GraphDSL.Implicits._
    val broadcast = builder.add(Broadcast[(CommittableMessage[String, String], Tweet)](2))
    batchedKafkaSource ~> broadcast ~> twitterSink
    broadcast ~> batchedSink
    ClosedShape
  }

  val batchedKafkaGraph = RunnableGraph.fromGraph(graph)

  val fromBeginningSource = Consumer.plainSource(consumerSettings, Subscriptions.assignmentWithOffset(
    new TopicPartition("topic1", 0), 0
  ))

  val offsetDB = new OffsetDB

  val externalOffsetStorage = offsetDB.loadOffset().foreach { fromOffset =>
    val partition = 0
    val subscription = Subscriptions.assignmentWithOffset(
      new TopicPartition("topic1", partition) -> fromOffset
    )

    Consumer.committableSource(consumerSettings, subscription)
      .mapAsync(producerSettings.parallelism) { record =>
        val extractOffset = { (message: CommittableMessage[String, String]) =>
          message.committableOffset.partitionOffset.offset
        }
        offsetDB.save[String, String, CommittableMessage, Done](record, extractOffset, Done)
      }.runWith(Sink.ignore)
  }
}
