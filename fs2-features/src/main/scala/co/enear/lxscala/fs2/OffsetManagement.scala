package co.enear.lxscala.fs2

import scala.concurrent.duration._

import fs2._
import fs2.util._

import co.enear.fs2.kafka._
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.TopicPartition

import co.enear.lxscala.twitter.util.TweetUtils.parseTweet
import DefaultSerialization._

object OffsetManagement extends App {

  // configuration stuff
  implicit val strategy = Strategy.fromCachedDaemonPool("workers")
  val bootstrapServers = "localhost:9092"
  val consumerGroup = "retweets"
  val consumerSettings = ConsumerSettings[String, String](50 millis)
    .withBootstrapServers(bootstrapServers)
    .withGroupId(consumerGroup)
    .withAutoCommit(false)
  val subscription = Subscriptions.topics("topic1")
  val producerSettings = ProducerSettings[String, String]()
    .withBootstrapServers(bootstrapServers)

  type Record = ConsumerRecord[String, String]

  // stream definition
  val kafkaStorageStream = Consumer[Task, String, String](consumerSettings)
    .simpleStream
    .commitableMessages(subscription)
    .map(_.map(r => parseTweet[Record](r, _.value)))
    // do stuff with tweet
    .map {
      case CommitableMessage(tweet, offset) => ProducerMessage(
        new ProducerRecord[String, String]("topic2", tweet.id_str, tweet.toString), offset
      )
    }
    .to(Producer[Task, String, String](producerSettings).sendCommitable)

  //
  val assignment = Subscriptions.assignmentWithOffsets(
    Map(new TopicPartition("topic1", 0) -> 0L)
  )
  val fromBeginningStream = Consumer[Task, String, String](consumerSettings)
    .simpleStream
    .plainMessages(subscription: Subscription)
  // do stuff

  val db = new MemoryDB[Task]

  val manualStorageStream = Stream.eval(db.load)
    .flatMap { offset =>
      val assignment = Subscriptions.assignmentWithOffsets(
        Map(new TopicPartition("topic1", 0) -> offset)
      )
      Consumer[Task, String, String](consumerSettings)
        .simpleStream
        .commitableMessages(assignment)
        // do stuff with message
        .evalMap(msg => db.save(msg.commitableOffset.partitionOffset.offset + 1))
    }

}

trait OffsetDB[F[_]] {
  def load: F[Long]
  def save(offset: Long): F[Unit]
}

class MemoryDB[F[_]](implicit F: Suspendable[F]) extends OffsetDB[F] {
  val offset = new java.util.concurrent.atomic.AtomicLong(0l)
  def load = F.delay {
    offset.get()
  }

  def save(newOffset: Long) = F.delay {
    offset.set(newOffset)
  }
}
