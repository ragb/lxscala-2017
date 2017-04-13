package co.enear.lxscala.twitter.entities

case class OEmbedTweet(
  author_name: Option[String],
  author_url: Option[String],
  cache_age: Option[String],
  height: Option[Int],
  html: String,
  provider_url: Option[String],
  provider_name: Option[String],
  title: Option[String],
  `type`: String,
  url: String,
  version: String,
  width: Option[Int]
)

