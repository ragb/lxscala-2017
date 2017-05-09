package co.enear.lxscala.fs2

import fs2._

object Fs2Example extends App {
  implicit val strategy = Strategy.fromFixedDaemonPool(8, "fs2")
  val addLineNumbersStream = io.stdin[Task](4096)
    .through(text.utf8Decode)
    .through(text.lines)
    .zipWithIndex
    .map { case (line, index) => s"${index + 1}: $line" }
    .through(text.utf8Encode)
    .to(io.stdout[Task])

  val addLineNumbersTask = addLineNumbersStream.run
  addLineNumbersTask.unsafeRun()

}
