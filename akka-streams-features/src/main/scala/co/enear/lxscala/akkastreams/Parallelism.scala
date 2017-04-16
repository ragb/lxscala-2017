package co.enear.lxscala.akkastreams

import akka.{ Done, NotUsed }
import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.{ CommittableMessage, CommittableOffset }
import akka.kafka.ProducerMessage.Message
import akka.kafka.scaladsl.{ Consumer, Producer }
import akka.kafka.{ ConsumerSettings, ProducerMessage, ProducerSettings, Subscriptions }
import akka.stream.{ ActorMaterializer, ActorMaterializerSettings, Supervision }
import akka.stream.scaladsl.{ Flow, Sink }
import org.apache.kafka.common.serialization.{ StringDeserializer, StringSerializer }
import io.circe.syntax._
import cats.implicits._
import co.enear.lxscala.twitter.entities.UserCount
import co.enear.lxscala.twitter.util.TweetUtils._
import co.enear.lxscala.twitter.exceptions.Exceptions._
import com.typesafe.scalalogging.LazyLogging
import io.circe.Encoder
import org.apache.kafka.clients.producer.ProducerRecord

import scala.concurrent.Future

object Parallelism extends App with LazyLogging {

  val decider: Supervision.Decider = (t: Throwable) => t match {
    case ParsingException(msg) =>
      logger.error(msg)
      Supervision.Stop
    case TweetWithoutUserException(msg) =>
      logger.error(msg)
      Supervision.Resume
    case e =>
      logger.error(e.getMessage)
      Supervision.Resume
  }

  implicit val system = ActorSystem("System")
  implicit val dispatcher = system.dispatcher
  val materializerSettings = ActorMaterializerSettings(system).withSupervisionStrategy(decider)
  implicit val materializer = ActorMaterializer(materializerSettings)

  val producerSettings = ProducerSettings(system, new StringSerializer, new StringSerializer)
  val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)

  type CommitMessage = CommittableMessage[String, String]
  type UserAggregation = (Long, (Int, Seq[CommitMessage]))
  type AggregatedMessage = (CommittableOffset, UserCount)

  def createProducerRecord[EncodableEntity](entity: EncodableEntity, topic: String)(implicit encoder: Encoder[EncodableEntity]) = {
    new ProducerRecord[String, String](topic, entity.asJson.toString)
  }

  def createProducerMessageWithOffset[EncodableEntity](
    offset: CommittableOffset,
    entity: EncodableEntity,
    topic: String
  )(implicit encoder: Encoder[EncodableEntity]): Message[String, String, CommittableOffset] = {
    ProducerMessage.Message(
      createProducerRecord(entity, topic),
      offset
    )
  }

  def streamWithPartitionPerSource[FlowOut, KafkaMessage](
    consumerTopic: String,
    producerTopic: String,
    logicFlow: Flow[CommitMessage, FlowOut, NotUsed],
    createKafkaMessage: (String, FlowOut) => KafkaMessage,
    sink: Sink[KafkaMessage, Future[Done]]
  ): Future[Done] = {
    Consumer.committablePartitionedSource(consumerSettings, Subscriptions.topics(consumerTopic))
      .map {
        case (topicPartition, source) =>
          source
            .via(logicFlow)
            .map { flowResponse => createKafkaMessage(producerTopic, flowResponse) }
            .runWith(sink)
      }
      .mapAsyncUnordered(producerSettings.parallelism)(identity)
      .runWith(Sink.ignore)
  }

  def streamWithMergedSources[FlowOut, KafkaMessage](
    consumerTopic: String,
    producerTopic: String,
    logicFlow: Flow[CommitMessage, FlowOut, NotUsed],
    createKafkaMessage: (String, FlowOut) => KafkaMessage,
    sink: Sink[KafkaMessage, Future[Done]]
  ): Future[Done] = {
    Consumer.committablePartitionedSource(consumerSettings, Subscriptions.topics(consumerTopic))
      .flatMapMerge(producerSettings.parallelism, _._2)
      .via(logicFlow)
      .map { flowResponse => createKafkaMessage(producerTopic, flowResponse) }
      .runWith(sink)
  }

  val tweetsPerUserWithCommitOffset: Flow[CommitMessage, AggregatedMessage, NotUsed] = Flow[CommitMessage]
    .map { message =>
      val tweet = parseTweet[CommitMessage](message, _.record.value())
      val userId = tweet.user.map(_.id).getOrElse(throw TweetWithoutUserException(noUserInTweetErrorMsg(tweet.id_str)))
      (message, userId)
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
        messages.map(message => (message.committableOffset, UserCount(userId, count))).toList
    }

  val distinctUsersWithCommitOffset: Flow[CommittableMessage[String, String], Int, NotUsed] = Flow[CommittableMessage[String, String]]
    .map { message =>
      val tweet = parseTweet[CommitMessage](message, _.record.value())
      val userId = tweet.user.map(_.id).getOrElse(throw TweetWithoutUserException(noUserInTweetErrorMsg(tweet.id_str)))
      (message, userId)
    }
    .mapAsync(producerSettings.parallelism) {
      case (message, userId) =>
        message.committableOffset.commitScaladsl().map(_ => userId)
    }
    .groupBy(100000000, identity)
    .fold(Map.empty[Long, Int]) {
      case (userTweetCounts, userId) =>
        userTweetCounts |+| Map(userId -> 1)
    }
    .map(_.keySet.size)
    .mergeSubstreams
    .reduce(_ + _)

  val numTweetsPerUserWithMergedSources: Future[Done] =
    streamWithMergedSources(
      "topic1",
      "topic2",
      tweetsPerUserWithCommitOffset,
      { (producerTopic: String, tweetsPerUser: AggregatedMessage) => createProducerMessageWithOffset(tweetsPerUser._1, tweetsPerUser._2, producerTopic) },
      Producer.commitableSink(producerSettings)
    )

  val numTweetsPerUserWithSourcePerPartition: Future[Done] =
    streamWithPartitionPerSource(
      "topic1",
      "topic2",
      tweetsPerUserWithCommitOffset,
      { (producerTopic: String, tweetsPerUser: AggregatedMessage) => createProducerMessageWithOffset(tweetsPerUser._1, tweetsPerUser._2, producerTopic) },
      Producer.commitableSink(producerSettings)
    )

  val distinctUsersNaiveWithMergedSources: Future[Done] =
    streamWithMergedSources(
      "topic1",
      "topic2",
      distinctUsersWithCommitOffset,
      { (producerTopic: String, userCount: Int) => createProducerRecord(userCount, producerTopic) },
      Producer.plainSink(producerSettings)
    )

  val distinctUsersNaiveWithSourcePerPartition: Future[Done] =
    streamWithPartitionPerSource(
      "topic1",
      "topic2",
      distinctUsersWithCommitOffset,
      { (producerTopic: String, userCount: Int) => createProducerRecord(userCount, producerTopic) },
      Producer.plainSink(producerSettings)
    )

}
