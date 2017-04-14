package co.enear.lxscala.twitter.entities

import io.circe.generic.JsonCodec

@JsonCodec case class Area(coordinates: Seq[Seq[Seq[Double]]], `type`: String)
