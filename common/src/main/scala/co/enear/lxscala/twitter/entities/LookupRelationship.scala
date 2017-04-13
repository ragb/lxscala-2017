package co.enear.lxscala.twitter.entities

case class LookupRelationship(
  connections: Seq[String] = Seq.empty,
  id: Long,
  id_str: String,
  name: String,
  screen_name: String
)
