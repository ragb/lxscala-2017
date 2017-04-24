package co.enear.lxscala.twitter.config

import org.zalando.grafter._
import org.zalando.grafter.macros._

final case class TwitterAuthConfig(
  consumerKey: String,
  consumerSecret: String,
  accessToken: String,
  accessSecret: String
)

@readers
final case class TwitterConfig(
  auth: TwitterAuthConfig
)

final case class KafkaProducerConfig(
  topicPrefix: String,
  tweetsTopic: String,
  bootstrapServers: String
)

@readers
final case class ApplicationConfig(
  twitter: TwitterConfig,
  kafka: KafkaProducerConfig
)

object ApplicationConfig extends GenericReader
