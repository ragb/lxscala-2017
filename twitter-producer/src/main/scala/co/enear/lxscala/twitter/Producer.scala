package co.enear.lxscala.twitter

import co.enear.lxscala.twitter.entities._
import co.enear.fs2.kafka.{ Producer => KafkaProducer, _ }
import co.enear.fs2.kafka.DefaultSerialization._
import org.apache.kafka.clients.producer.ProducerRecord

import co.enear.lxscala.twitter.config._
import org.zalando.grafter.macros._
import io.circe._
import io.circe.syntax._
import fs2._
import com.typesafe.scalalogging._

@dependentReader
final case class TweetProducer(
    producerConfig: KafkaProducerConfig
) extends LazyLogging {

  def logJsonSink: Sink[Task, Json] = _.evalMap { json =>
    Task.delay {
      logger.debug(s"Writing $json")
    }
  }

  def tweetSink(implicit strategy: Strategy): Sink[Task, Tweet] = { s: Stream[Task, Tweet] =>
    val settings = ProducerSettings[String, String]()
      .withBootstrapServers(producerConfig.bootstrapServers)
    KafkaProducer[Task, String, String, Unit](settings) { producer =>
      s.map(tweet => (tweet.user.get.screen_name, tweet.asJson))
        .observe[Task, (String, Json)](_.map(_._2).to(logJsonSink))
        .map { case (key, json) => ProducerMessage[String, String, Unit](new ProducerRecord(s"${producerConfig.topicPrefix}${producerConfig.tweetsTopic}", key, json.noSpaces), ()) }
        .map { e => println(e); e }
        .through(producer.send)
        .drain

    }
  }
}
