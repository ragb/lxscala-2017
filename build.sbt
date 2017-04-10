import Dependencies._
import Slides._

lazy val root = (project in file(".")).
  aggregate(twitterProducer, common, slides)

val commonSettings =  Seq(
      organization := "co.enear",
  scalaVersion := "2.11.8",
  //cancelable in Global := true,
      version      := "0.1.0-SNAPSHOT",
  resolvers ++= Seq(
    Resolver.sonatypeRepo("snapshots"),
    rossabackerBintray,
    ragbBintray
  ),
    libraryDependencies ++= Seq(
      specs2Core % Test
    ),
      scalacOptions ++= Seq(
  "-deprecation",           
  "-encoding", "UTF-8",       // yes, this is 2 args
  "-feature",                
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Xfatal-warnings",       
  "-Xlint",
  "-Yno-adapted-args",       
  "-Ywarn-dead-code",        // N.B. doesn't work well with the ??? hole
  "-Ywarn-numeric-widen",   
  "-Ywarn-value-discard",
  "-Xfuture",
  "-Ywarn-unused-import"     // 2.11 only
)
  )



lazy val common = (project in file("common")).
settings(commonSettings:_*).
settings(name := "lxscala-common")

lazy val twitterProducer = (project in file("twitter-producer")).
  settings(commonSettings:_*).
  settings(
    name := "twitter-producer",
    libraryDependencies ++= Seq(
      fs2core,
      http4sBlazeClient,
      http4sDsl,
      jawnFs2,
      circeCore,
      circeParser,
      circeGeneric,
      logback,
      scalaLogging,
      pureconfig,
      grafter,
      macroParadise
    )
  ).
  dependsOn(common)

lazy val slides = (project in file("slides")).
  settings(commonSettings:_*).
  settings(
    name := "slides",
    libraryDependencies += revealjs
  ).
  settings(tutSettings:_*).
  settings(slidesSettings:_*).
  settings(
    slidesSourceFile := tutTargetDirectory.value / "slides.md",
    slidesHtml := (slidesHtml.dependsOn(tut)).value
  ).
  dependsOn(common).
  enablePlugins(SbtWeb)

