package co.enear.lxscala.twitter.entities

case class Users(users: Seq[User] = Seq.empty, next_cursor: Long, previous_cursor: Long)
