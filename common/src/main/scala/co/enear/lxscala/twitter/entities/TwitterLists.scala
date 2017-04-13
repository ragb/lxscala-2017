package co.enear.lxscala.twitter.entities

case class TwitterLists(lists: Seq[TwitterList] = Seq.empty, next_cursor: Long, previous_cursor: Long)
