package co.enear.lxscala.twitter

import fs2._
import com.typesafe.scalalogging.LazyLogging
import org.zalando.grafter.macros._
import config.ApplicationConfig

@reader[ApplicationConfig]
final case class Application(twitterClient: TwitterStreamingClient, producer: Producer) extends LazyLogging {
  def run: Task[Unit] = twitterClient
    .jsonStream(Query.PhraseQuery("Trump"))
    .to(producer.statusSink)
    .run
}
