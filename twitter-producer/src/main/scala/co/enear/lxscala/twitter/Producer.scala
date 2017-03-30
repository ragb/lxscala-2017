package co.enear.lxscala.twitter

import org.zalando.grafter.macros._
import io.circe._
import fs2._
import com.typesafe.scalalogging._

@dependentReader
final case class Producer() extends LazyLogging {
  def statusSink: Sink[Task, Json] = _.evalMap { json =>
    Task.delay {
      logger.debug(s"Writing $json")
    }
  }
}
