package co.enear.lxscala.twitter

import org.zalando.grafter.GenericReader
import shapeless._
import cats.data.Reader

package object config {
  type ConfigReader[A] = Reader[ApplicationConfig, A]
  import GenericReader._

  def createReader[A, B](implicit gen: LabelledGeneric.Aux[A, B], repr: Lazy[ConfigReader[B]]): ConfigReader[A] =
    genericReader[ApplicationConfig, A, B](gen, repr)

  def configure[A](c: ApplicationConfig)(implicit r: ConfigReader[A]): A =
    r.run(c)

}
