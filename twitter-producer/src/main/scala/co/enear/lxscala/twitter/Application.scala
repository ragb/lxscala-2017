package co.enear.lxscala.twitter

import io.circe._
import fs2._
import com.typesafe.scalalogging.LazyLogging
import org.zalando.grafter.macros._
import config.ApplicationConfig
import entities.Tweet

@reader[ApplicationConfig]
final case class Application(twitterClient: TwitterStreamingClient, producer: TweetProducer) extends LazyLogging {
  implicit val strategy = Strategy.fromCachedDaemonPool("worker")
  def run: Task[Unit] = twitterClient
    .jsonStream(Query.PhraseQuery("Trump"))
    .map(_.as[Tweet])
    .observe(logJsonErrorsSink[Tweet])
    .collect { case Right(v) => v }
    .to(producer.tweetSink)
    .run

  def logJsonErrorsSink[T]: Sink[Task, Decoder.Result[T]] = _.collect {
    case Left(error) =>
      logger.warn(s"Error decoding json: $error")
  }

}
