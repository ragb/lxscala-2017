package co.enear.lxscala.akkastreams

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.kafka.scaladsl._
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ ByteArraySerializer, StringSerializer }

object ReadWrite extends App {
  implicit val system = ActorSystem("System")

  implicit val materializer = ActorMaterializer()

  val producerSettings = ProducerSettings(system, new ByteArraySerializer, new StringSerializer)

  Source(1 to 100)
    .map(_.toString)
    .map { elem =>
      new ProducerRecord[Array[Byte], String]("topic1", elem)
    }
    .runWith(Producer.plainSink(producerSettings))

}
