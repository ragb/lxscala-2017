package co.enear.lxscala.akkastreams

import akka.actor.ActorSystem
import akka.kafka.{ ConsumerSettings, ProducerSettings, Subscriptions }
import akka.stream.{ ActorAttributes, ActorMaterializer, Supervision }
import akka.kafka.scaladsl._
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ StringDeserializer, StringSerializer }
import co.enear.lxscala.twitter.util.TweetUtils._
import org.apache.kafka.clients.consumer.ConsumerRecord

object ReadWrite extends App {
  implicit val system = ActorSystem("System")
  implicit val dispatcher = system.dispatcher

  implicit val materializer = ActorMaterializer()

  val producerSettings = ProducerSettings(system, new StringSerializer, new StringSerializer)
  val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)

  Consumer.plainSource(consumerSettings, Subscriptions.topics("topic1"))
    .map { record =>
      parseTweet[ConsumerRecord[String, String]](record, _.value())
    }.map { tweet =>
      new ProducerRecord[String, String]("topic2", tweet.retweet_count.toString)
    }.withAttributes(
      ActorAttributes.supervisionStrategy(Supervision.resumingDecider)
    ).runWith(Producer.plainSink(producerSettings))

}
