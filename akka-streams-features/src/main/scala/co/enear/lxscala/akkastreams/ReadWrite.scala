package co.enear.lxscala.akkastreams

import akka.actor.ActorSystem
import akka.kafka.{ ConsumerSettings, ProducerSettings, Subscriptions }
import akka.stream.{ ActorAttributes, ActorMaterializer, Supervision }
import akka.kafka.scaladsl._
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.{ StringDeserializer, StringSerializer }
import io.circe.Json
import io.circe.parser._
import io.circe.generic.semiauto._
import cats.implicits._
import co.enear.lxscala.twitter.exceptions.ParsingException

object ReadWrite extends App {
  implicit val system = ActorSystem("System")
  implicit val dispatcher = system.dispatcher

  implicit val materializer = ActorMaterializer()

  case class Tweett(id: Int, retweet_count: Long)
  implicit val tweetDecoder = deriveDecoder[Tweett]

  val producerSettings = ProducerSettings(system, new StringSerializer, new StringSerializer)
  val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)

  Consumer.plainSource(consumerSettings, Subscriptions.assignmentWithOffset(
    new TopicPartition("topic1", 0), 0
  )).map { record =>
    parse(record.value)
      .getOrElse(Json.Null).as[Tweett]
      .fold(decodingFailure => throw ParsingException(decodingFailure.message), tweet => tweet)
  }.map { tweet =>
    new ProducerRecord[String, String]("topic1", tweet.retweet_count.toString)
  }.withAttributes(
    ActorAttributes.supervisionStrategy(Supervision.resumingDecider)
  ).runWith(Producer.plainSink(producerSettings))

}
