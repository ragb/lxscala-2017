package co.enear.lxscala.fs2

import cats.implicits._
import scala.concurrent.duration._

import fs2._
import fs2.async.mutable.Signal

import org.apache.kafka.clients.consumer.ConsumerRecord
import co.enear.fs2.kafka._

import DefaultSerialization._

object Parallelism {

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

  // expensive computation to compute a key from an element
  type KeyFunc[F[_], E] = E => F[String]

  // consumes from kafka
  // gets a key from a message
  // aggregates counts
  // and writes them to a signal that can be assynchronously checked
  def parallelCount(keyFunc: KeyFunc[Task, CommitableMessage[Task, ConsumerRecord[String, String]]], signal: Signal[Task, Map[String, Int]]) = {
    val partitioned = Consumer[Task, String, String](consumerSettings)
      .partitionedStreams
      .commitableMessages(Subscriptions.topics("topic1"))
      .map {
        case (_, innerStream) =>
          innerStream.evalMap(keyFunc)
      }
    // join streams and aggregate key counts
    fs2.concurrent.join(100)(partitioned)
      .scan(Map.empty[String, Int]) { case (current, key) => current |+| Map(key -> 1) }
      .evalMap(signal.set _)
  }

}
