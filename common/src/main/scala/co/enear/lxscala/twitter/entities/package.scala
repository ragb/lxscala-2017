package co.enear.lxscala.twitter

import io.circe.generic.extras.{ Configuration => CirceConfiguration }

package object entities {
  implicit val jsonConfiguration = CirceConfiguration.default.withDefaults
}
