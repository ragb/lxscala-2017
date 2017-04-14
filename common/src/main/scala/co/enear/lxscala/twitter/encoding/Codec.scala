package co.enear.lxscala.twitter.encoding

import java.time.Instant
import java.util.Date

import cats.implicits._
import io.circe.{ Decoder, Encoder }

object Codec {
  implicit val dateEncoder: Encoder[Date] = Encoder.encodeString.contramap[Date](_.toString)

  implicit val dateDecoder: Decoder[Date] = Decoder.decodeString.emap { str =>
    Either.catchNonFatal(Date.from(Instant.parse(str))).leftMap(t => "Date")
  }
}
