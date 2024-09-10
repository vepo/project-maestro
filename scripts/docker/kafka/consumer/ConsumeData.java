///usr/bin/env jbang "$0" "$@" ; exit $? 
//DEPS org.apache.kafka:kafka-clients:3.8.0
//DEPS org.slf4j:slf4j-simple:2.0.16
//DEPS org.slf4j:slf4j-api:2.0.16

import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Arrays;
import java.time.Duration;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

public class ConsumeData {
    public static void main(String[] args) {
        var configs = new Properties();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-0:9092, kafka-1:9094, kafka-2:9096");
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, "meu-consumer");
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        var running = new AtomicBoolean(true);
        var latch = new CountDownLatch(1);
        Runtime.getRuntime()
               .addShutdownHook(new Thread() {
                   @Override
                   public void run() {
                       running.set(false);
                       try {
                           latch.await();
                       } catch (InterruptedException e) {
                           Thread.currentThread().interrupt();
                       }
                   }
               });
        try(var consumer = new KafkaConsumer<String, String>(configs)) {
            consumer.subscribe(Arrays.asList("topic"));
            while(running.get()) {
                var records = consumer.poll(Duration.ofMillis(1000));
                records.forEach(record -> System.out.println("Record: " + record.value()));
            }
        }
        latch.countDown();
    }
}
