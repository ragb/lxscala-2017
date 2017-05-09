## fs2-kafka

http://github.com/ragb/fs2-kafka


## Generic Interface


```scala
trait Consumer[F[_], K, V] {

  private[kafka] def createConsumer: F[ConsumerControl[F, K, V]]

  trait StreamType {
    type OutStreamType[_] <: Stream[F, _]
    private[kafka] def makeStream(
      subscription: Subscription,
      builder: MessageBuilder[F, K, V]
    )(implicit F: Async[F]): OutStreamType[builder.Message]

    def commitableMessages(
      subscription: Subscription
    )(implicit F: Async[F]): OutStreamType[CommitableMessage[F, ConsumerRecord[K, V]]] = makeStream(subscription, new CommitableMessageBuilder[F, K, V])
  }
  
  val simpleStream = new StreamType {
    type OutStreamType[A] = Stream[F, A]
    // ...
  }

  val partitionedStreams = new StreamType {
    type OutStreamType[A] = Stream[F, (TopicPartition, Stream[F, A])]
    // ...

  }

}
```



## Resource aquisition

```scala
  def bracket[F[_],R,A](r: F[R])(use: R => Stream[F,A], release: R => F[Unit]): Stream[F,A]
```


## Back pressure


## State management

Mutable queues and refs:

```scala
openPartitions: Async.Ref[F, Map[TopicPartition, Queue[F, Option[Chunk[ConsumerRecord[K, V]]]]]]
```

Notify assigned partitions:

```scala
openPartitionsQueue: Queue[F, (TopicPartition, Stream[F, ConsumerRecord[K, V]])]
```



## Producer

```scala
trait Producer[F[_], K, V] {
  //...
  def send[P](implicit F: Async[F]): Pipe[F, ProducerMessage[K, V, P], ProducerMetadata[P]]
  def sendAsync: Sink[F, ProducerRecord[K, V]]
}
```

