import sbt._
import sbt.Keys._
import sbt.Process.stringToProcess
import language.postfixOps

object Slides {
  lazy val slidesSourceFile = SettingKey[File]("Slides markdown file")
  lazy val slidesTargetDirectory = SettingKey[File]("Slides output directory")
  lazy val slidesHtmlIndexName = SettingKey[String]("Slides html index file name")
  lazy val slidesRevealjsTheme = SettingKey[String]("slides reveal.js theme")
  lazy val slidesHtml = TaskKey[Seq[File]]("slideshtml", "Produce slides in html format")
  lazy val slidesSettings = Seq(
    slidesTargetDirectory := target.value / "slides",
    slidesRevealjsTheme := "white",
    slidesHtmlIndexName := {
      val (base, ext) = IO.split(slidesSourceFile.value.getName)
      s"$base.html"
    },
    slidesHtml  := {
      slidesTargetDirectory.value.mkdirs()
      val outputFile = slidesTargetDirectory.value / slidesHtmlIndexName.value
      streams.value.log.info(s"Writing slides html to $outputFile")
      s"pandoc --variable=theme:${slidesRevealjsTheme.value} --variable=revealjs-url:lib/reveal.js --slide-level=2 --standalone -t revealjs -o $outputFile ${slidesSourceFile.value}" !;
      Seq(outputFile)
    }
  )
}
