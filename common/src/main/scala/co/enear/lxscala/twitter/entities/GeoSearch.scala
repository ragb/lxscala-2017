package co.enear.lxscala.twitter.entities

case class GeoSearch(query: GeoQuery, result: GeoResult)

case class GeoQuery(params: GeoParams, `type`: String, url: String)

case class GeoResult(places: Seq[GeoPlace] = Seq.empty)

case class GeoParams(
  accuracy: String,
  granularity: String,
  coordinates: Option[Coordinates] = None,
  query: Option[String] = None,
  autocomplete: Boolean = false,
  trim_place: Boolean = false
)

