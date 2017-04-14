package co.enear.lxscala.twitter.entities

import io.circe.generic.JsonCodec

@JsonCodec case class HashTag(text: String, indices: Seq[Int])
