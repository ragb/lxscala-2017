package co.enear.lxscala.akkastreams

import akka.kafka.ConsumerMessage.CommittableOffset
import akka.kafka.ProducerMessage
import akka.kafka.ProducerMessage.Message
import io.circe.Encoder
import io.circe.syntax._
import org.apache.kafka.clients.producer.ProducerRecord

object KafkaUtils {
  def createProducerRecord[EncodableEntity](entity: EncodableEntity, topic: String)(implicit encoder: Encoder[EncodableEntity]) = {
    new ProducerRecord[String, String](topic, entity.asJson.toString)
  }

  def createProducerMessageWithOffset[EncodableEntity](
    offset: CommittableOffset,
    entity: EncodableEntity,
    topic: String
  )(implicit encoder: Encoder[EncodableEntity]): Message[String, String, CommittableOffset] = {
    ProducerMessage.Message(
      createProducerRecord(entity, topic),
      offset
    )
  }
}
