// Initialization
Properties props = new Properties();
props.put("bootstrap.servers", "localhost:9092");
props.put("group.id", "consumer-tutorial");
props.put("key.deserializer", StringDeserializer.class.getName());
props.put("value.deserializer", StringDeserializer.class.getName());
KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props); 


// topic subscription
consumer.subscribe(Arrays.asList(“foo”, “bar”)); 

// Poll
// Single-threaded, shutdown from other thread
try {
  while (true) {
    ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);
    for (ConsumerRecord<String, String> record : records)
      System.out.println(record.offset() + “: ” + record.value());
  }
} catch (WakeupException e) {
  // ignore for shutdown
} finally {
  consumer.close();
}

// shutdown:
    consumer.wakeup();
