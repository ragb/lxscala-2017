
## Initialization
```scala
val props = new Properties()
props.put("bootstrap.servers", "localhost:9092")
props.put("group.id", "consumer-tutorial")
props.put("key.deserializer", StringDeserializer.class.getName())
props.put("value.deserializer", StringDeserializer.class.getName())
consumer = new KafkaConsumer[String, String](props)

consumer.subscribe(List("foo", "bar").asJava)
```

### Notes

- property based configuration
- reflection
- Java API (even with scala help)



## Polling loop

In one thread:

```scala
try {
  while (true) {
    val records = consumer.poll(Long.MAX_VALUE);
    records.iterator.asScala foreach {record => println(record.value)}
    
} catch {
  case e: WakeupException => // ignore for shutdown
} finally {
  consumer.close()
}
```

In another thread, to shutdown:

```scala
consumer.wakeup()
```

### Notes

- Exceptions as control flow
- Blocking API
- Single threaded access (external synchronization)