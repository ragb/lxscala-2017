package co.enear.lxscala.twitter.entities

case class Suggestions(
  name: String,
  slug: String,
  size: Int,
  users: Seq[User] = Seq.empty
)
