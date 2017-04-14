package co.enear.lxscala.twitter.entities

import io.circe.generic.JsonCodec

@JsonCodec case class Coordinates(coordinates: Seq[Double] = Seq.empty, `type`: String)
