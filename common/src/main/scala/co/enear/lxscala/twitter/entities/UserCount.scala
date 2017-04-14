package co.enear.lxscala.twitter.entities

import io.circe.generic.JsonCodec

@JsonCodec case class UserCount(userId: Long, count: Int)
