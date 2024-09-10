///usr/bin/env jbang "$0" "$@" ; exit $? 
//DEPS org.apache.kafka:kafka-clients:3.8.0
//DEPS org.slf4j:slf4j-simple:2.0.16
//DEPS org.slf4j:slf4j-api:2.0.16

import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ExecutionException;

public class ProduceData {
    public static void main(String[] args) {
        var configs = new Properties();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-0:9092, kafka-1:9094, kafka-2:9096");
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        var running = new AtomicBoolean(true);
        Runtime.getRuntime()
               .addShutdownHook(new Thread() {
                   @Override
                   public void run() {
                       running.set(false);
                   }
               });
        try(var producer = new KafkaProducer<String, String>(configs)) {
            var record = new ProducerRecord<String, String>("topic", "key", "value");
            while (running.get()) {
                try {
                    var metadata = producer.send(record).get();
                    System.out.println("Record sent to partition " + metadata.partition() + " with offset " + metadata.offset());
                    Thread.sleep(1000);
                } catch (InterruptedException | ExecutionException e) {
                    running.set(false);
                }
            }
        }
        System.out.println("Done!");
    }
}
