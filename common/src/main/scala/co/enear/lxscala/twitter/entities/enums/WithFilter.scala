package co.enear.lxscala.twitter.entities.enums

object WithFilter extends Enumeration {
  type WithFilter = Value

  val User = Value("user")
  val Followings = Value("followings")
}
