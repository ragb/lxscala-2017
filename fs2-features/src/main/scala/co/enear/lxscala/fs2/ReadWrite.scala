package co.enear.lxscala.fs2

import scala.concurrent.duration._

import fs2._
import co.enear.fs2.kafka._
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord

import co.enear.lxscala.twitter.util.TweetUtils.parseTweet
import DefaultSerialization._

object ReadWrite extends App {
  // configuration stuff
  implicit val strategy = Strategy.fromCachedDaemonPool("workers")
  val bootstrapServers = "localhost:9092"
  val consumerGroup = "retweets"
  val consumerSettings = ConsumerSettings[String, String](50 millis)
    .withBootstrapServers(bootstrapServers)
    .withGroupId(consumerGroup)
  val subscription = Subscriptions.topics("topic1")
  val producerSettings = ProducerSettings[String, String]()
    .withBootstrapServers(bootstrapServers)

  // stream definition
  val stream = Consumer[Task, String, String](consumerSettings)
    .simpleStream
    .plainMessages(subscription)
    .map(msg => parseTweet[ConsumerRecord[String, String]](msg, _.value))
    .map(_.retweet_count)
    .map(count => new ProducerRecord[String, String]("topic2", "key", count.toString))
    .to(Producer[Task, String, String](producerSettings).sendAsync)

  // run at end of universe
  stream.run.unsafeRun()

}
