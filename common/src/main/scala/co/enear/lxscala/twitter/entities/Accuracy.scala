package co.enear.lxscala.twitter.entities

import co.enear.lxscala.twitter.entities.enums.Measure
import co.enear.lxscala.twitter.entities.enums.Measure.Measure

case class Accuracy(amount: Int, unit: Measure) {
  override def toString = s"$amount$unit"
}

object Accuracy {
  val Default = Accuracy(0, Measure.Meter)
}
