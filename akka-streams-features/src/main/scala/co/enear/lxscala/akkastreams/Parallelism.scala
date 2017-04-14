package co.enear.lxscala.akkastreams

import akka.{ Done, NotUsed }
import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.{ CommittableMessage, CommittableOffset }
import akka.kafka.ProducerMessage.Message
import akka.kafka.scaladsl.{ Consumer, Producer }
import akka.kafka.{ ConsumerSettings, ProducerMessage, ProducerSettings, Subscriptions }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Flow, Sink }
import org.apache.kafka.common.serialization.{ StringDeserializer, StringSerializer }
import io.circe.parser._
import io.circe.syntax._
import cats.implicits._
import co.enear.lxscala.twitter.entities.{ Tweet, UserCount }
import co.enear.lxscala.twitter.exceptions.ParsingException
import org.apache.kafka.clients.producer.ProducerRecord

import scala.concurrent.Future

object Parallelism {
  implicit val system = ActorSystem("System")
  implicit val dispatcher = system.dispatcher

  implicit val materializer = ActorMaterializer()

  val producerSettings = ProducerSettings(system, new StringSerializer, new StringSerializer)
  val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)

  type CommitMessage = CommittableMessage[String, String]
  type UserAggregation = (Long, (Int, Seq[CommitMessage]))
  type AggregatedMessage = (CommittableOffset, Long, Int)

  def createProducerMessageFromUserCount(offset: CommittableOffset, userId: Long, count: Int, topic: String): Message[String, String, CommittableOffset] = {
    ProducerMessage.Message(
      new ProducerRecord[String, String](topic, UserCount(userId, count).asJson.toString),
      offset
    )
  }

  val tweetsPerUserWithCommitOffset: Flow[CommitMessage, AggregatedMessage, NotUsed] = Flow[CommittableMessage[String, String]]
    .map { message =>
      val tweetJS = parse(message.record.value()).getOrElse(throw new ParsingException(""))
      val tweet = tweetJS.as[Tweet].getOrElse(throw new ParsingException(""))
      (message, tweet.user.getOrElse(???).id)
    }
    .groupBy(1000000, _._2)
    .map { case (message, userId) => (userId, (1, Seq(message))) }
    .reduce[UserAggregation] {
      case ((leftUserId, (leftCount, leftMessageSeq)), (_, (rightCount, rightMessageSeq))) =>
        (leftUserId, (leftCount + rightCount, leftMessageSeq ++ rightMessageSeq))
    }
    .mergeSubstreams
    .mapConcat {
      case (userId, (count, messages)) =>
        messages.map(message => (message.committableOffset, userId, count)).toList
    }

  val numTweetsPerUserWithMergedSources: Future[Done] = Consumer.committablePartitionedSource(consumerSettings, Subscriptions.topics("topic1"))
    .flatMapMerge(producerSettings.parallelism, _._2)
    .via(tweetsPerUserWithCommitOffset)
    .map {
      case (commitableOffset, userId, count) =>
        createProducerMessageFromUserCount(commitableOffset, userId, count, "topic2")
    }
    .runWith(Producer.commitableSink(producerSettings))

  val numTweetsPerUserWithSourcePerPartition: Future[Done] = Consumer.committablePartitionedSource(consumerSettings, Subscriptions.topics("topic1"))
    .map {
      case (topicPartition, source) =>
        source
          .via(tweetsPerUserWithCommitOffset)
          .map {
            case (commitableOffset, userId, count) =>
              createProducerMessageFromUserCount(commitableOffset, userId, count, "topic2")
          }
          .runWith(Producer.commitableSink(producerSettings))
    }
    .mapAsyncUnordered(producerSettings.parallelism)(identity)
    .runWith(Sink.ignore)
}
