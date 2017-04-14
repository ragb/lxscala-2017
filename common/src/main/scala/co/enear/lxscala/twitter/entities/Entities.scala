package co.enear.lxscala.twitter.entities

import io.circe.generic.JsonCodec

// TODO - conversion to clean this representation? see url vs urls
// unfortunately Twitter is not consistent when representing this entity...

@JsonCodec case class Entities(
  hashtags: Seq[HashTag] = Seq.empty,
  media: Seq[Media] = Seq.empty,
  url: Option[Urls] = None,
  urls: Seq[UrlDetails] = Seq.empty,
  user_mentions: Seq[UserMention] = Seq.empty,
  description: Option[Urls] = None
)

@JsonCodec case class Urls(urls: Seq[UrlDetails] = Seq.empty)

@JsonCodec case class UrlDetails(url: String, expanded_url: String, display_url: String, indices: Seq[Int])

