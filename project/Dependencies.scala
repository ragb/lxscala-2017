import sbt._


object Dependencies {
  lazy val logback ="ch.qos.logback" % "logback-classic" % logbackVersion
  lazy val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion
  lazy val http4sBlazeClient = "org.http4s" %% "http4s-blaze-client" % http4sVersion changing()
  lazy val http4sDsl = "org.http4s" %% "http4s-dsl" % http4sVersion changing()
  lazy val fs2core = "co.fs2" %% "fs2-core" % fs2version
  lazy val jawnFs2 = "org.http4s" %% "jawn-fs2" % jawnFs2version

  // Circe
  lazy val Seq(circeCore, circeParser, circeGeneric) = Seq("circe-core", "circe-parser", "circe-generic") map(lib => "io.circe" %% lib % circeVersion)
  lazy val pureconfig = "com.github.melrief" %% "pureconfig" % pureconfigVersion
  lazy val grafter = "org.zalando" %% "grafter" % grafterVersion

// testing
  lazy val specs2Core = "org.specs2" %% "specs2-core" % specs2Version

// compiler plugins
lazy val macroParadise = compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

// Library versions
    val logbackVersion = "1.2.1"
  val scalaLoggingVersion = "3.5.0"
  val http4sVersion = "0.17.0-SNAPSHOT"
  val circeVersion = "0.7.0"
  val fs2version = "0.9.4"
  val jawnFs2version = "0.10.1"
  val specs2Version = "3.8.9"
  val pureconfigVersion = "0.6.0"
val grafterVersion = "1.4.8"

// Resolvers
val rossabackerBintray = "bintray/rossabaker" at "http://dl.bintray.com/rossabaker/maven"
}
