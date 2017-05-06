## Java API

- Java API
- Simple
- But Java


## Consumer

- Fetches records from a Kafka cluster
- Polling loop

## Initialization

```java
Properties props = new Properties();
props.put("bootstrap.servers", "localhost:9092");
props.put("group.id", "consumer-tutorial");
props.put("key.deserializer", StringDeserializer.class.getName());
props.put("value.deserializer", StringDeserializer.class.getName());
KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
```

## Initialization

- Property based configuration
- Reflection


## Subscription


```java
consumer.subscribe(Arrays.asList(?foo?, ?bar?));
```

## Polling loop

In one thread:

```java
try {
  while (true) {
    ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);
    for (ConsumerRecord<String, String> record : records)
      System.out.println(record.offset() + ?: ? + record.value());
  }
} catch (WakeupException e) {
  // ignore for shutdown
} finally {
  consumer.close();
}
```

## Polling loop

In another thread, to shutdown:

```java
consumer.wakeup();
```


## Points

- Single threaded access
- Blocking API
- `poll`takes care of heartbeats, commits,...
- Exceptions as control flow :(


## Producer

- Produces records to the kafka cluster

## API

- Initialization similar to consumer
- Can be used from various threads

## API

```java
public Future<RecordMetadata> send(ProducerRecord<K,V> record,
                          Callback callback)
```

- Adds records to send buffer
- Assynchronous confirmation (if needed)


