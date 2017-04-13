package co.enear.lxscala.twitter.entities

case class GeoCode(latitude: Double, longitude: Double, radius: Accuracy) {
  override def toString = s"$latitude,$longitude,$radius"
}
