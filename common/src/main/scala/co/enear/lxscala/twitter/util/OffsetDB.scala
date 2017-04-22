package co.enear.lxscala.twitter.util

import java.util.concurrent.atomic.AtomicLong
import scala.concurrent.Future

class OffsetDB {

  private val offset = new AtomicLong

  def save[KeyType, ValueType, KafkaRecord[_, _], ReturnType](
    record: KafkaRecord[KeyType, ValueType],
    getOffset: KafkaRecord[KeyType, ValueType] => Long,
    transform: => ReturnType
  ): Future[ReturnType] = {
    offset.set(getOffset(record))
    Future.successful(transform)
  }

  def loadOffset(): Future[Long] =
    Future.successful(offset.get)

}
