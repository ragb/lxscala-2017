package co.enear.lxscala.twitter.entities

case class Location(
  country: String,
  countryCode: Option[String],
  name: String,
  parentid: Long,
  placeType: PlaceType,
  url: String,
  woeid: Long
)
