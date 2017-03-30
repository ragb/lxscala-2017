package co.enear.lxscala.twitter

import com.typesafe.scalalogging._

import config._

object Main extends App with LazyLogging {
  pureconfig.loadConfig[ApplicationConfig] match {
    case Left(error) => logger.error(s"error loading configuration $error")
    case Right(applicationConfig) =>
      val application = configure[Application](applicationConfig)
      logger.info("Starting stream")
      application.run.unsafeRun()
  }

}
