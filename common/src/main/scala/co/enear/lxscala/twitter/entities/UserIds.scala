package co.enear.lxscala.twitter.entities

case class UserIds(ids: Seq[Long] = Seq.empty, next_cursor: Long, previous_cursor: Long)
