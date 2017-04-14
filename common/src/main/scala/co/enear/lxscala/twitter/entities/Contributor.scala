package co.enear.lxscala.twitter.entities

import io.circe.generic.JsonCodec

@JsonCodec case class Contributor(id: Long, id_str: String, screen_name: String)
