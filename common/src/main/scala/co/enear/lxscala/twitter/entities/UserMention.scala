package co.enear.lxscala.twitter.entities

case class UserMention(
  id: Long,
  id_str: String,
  indices: Seq[Int] = Seq.empty,
  name: String,
  screen_name: String
)
