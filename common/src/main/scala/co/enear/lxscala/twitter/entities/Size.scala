package co.enear.lxscala.twitter.entities

import io.circe.generic.JsonCodec

@JsonCodec case class Size(h: Int, resize: String, w: Int)
