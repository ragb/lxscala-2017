package co.enear.lxscala.twitter.entities

import io.circe.generic.JsonCodec

@JsonCodec case class TweetId(id: Long, id_str: String)
