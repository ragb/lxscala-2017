import sbt._
import sbt.Keys._
import Process.stringToProcess

object Slides {
  lazy val slidesSourceFile = SettingKey[File]("Slides markdown file")
  lazy val slidesTargetDirectory = SettingKey[File]("Slides output directory")
  lazy val slidesHtmlIndexName = SettingKey[String]("Slides html index file name")
  lazy val slidesHtml = TaskKey[Unit]("slideshtml", "Produce slides in html format")
  lazy val slidesSettings = Seq(
    slidesTargetDirectory := target.value / "slides",
    slidesHtmlIndexName := {
      val (base, ext) = IO.split(slidesSourceFile.value.getName)
      s"$base.html"
    },
    slidesHtml := {
      IO.createDirectory(slidesTargetDirectory.value)
      val outputFile = slidesTargetDirectory.value / slidesHtmlIndexName.value
      s"pandoc --self-contained --slide-level=2 -t slidy -o $outputFile ${slidesSourceFile.value}" !
    }
  )

}
