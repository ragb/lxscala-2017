package co.enear.lxscala.twitter.entities

import co.enear.lxscala.twitter.entities.enums.Mode.Mode

case class TwitterListUpdate(
  description: Option[String] = None,
  mode: Option[Mode] = None,
  name: Option[String] = None
)
