package co.enear.lxscala.twitter.encoding

import java.time._
import java.time.format._
import java.util.{ Date, Locale }

import cats.implicits._
import io.circe.{ Decoder, Encoder }

object Codec {
  private val utcDateFormater = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss ZZ yyyy", Locale.ENGLISH)

  implicit val dateEncoder: Encoder[Date] = Encoder.encodeString.contramap[Date] { d => utcDateFormater.format(d.toInstant()) }

  implicit val dateDecoder: Decoder[Date] = Decoder.decodeString.emap { str =>
    Either.catchNonFatal(Date.from(ZonedDateTime.parse(str, utcDateFormater).toInstant())).leftMap(t => "Date")
  }
}
