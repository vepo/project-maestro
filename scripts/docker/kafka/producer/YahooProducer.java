///usr/bin/env jbang "$0" "$@" ; exit $? 

//DEPS org.apache.kafka:kafka-clients:3.8.0
//DEPS org.slf4j:slf4j-simple:2.0.16
//DEPS org.slf4j:slf4j-api:2.0.16
//DEPS com.google.protobuf:protobuf-java:4.28.2
//DEPS com.google.protobuf:protobuf-java-util:4.28.2
//DEPS com.fasterxml.jackson.core:jackson-databind:2.17.2
//SOURCES model/PricingDataOuterClass.java

import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.URI;
import java.net.URISyntaxException;

import java.nio.ByteBuffer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.Base64;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.PricingDataOuterClass.PricingData;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.TextFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.protobuf.util.JsonFormat; 



public class YahooProducer {
    private static final Logger logger = LoggerFactory.getLogger(YahooProducer.class);
    private static CountDownLatch startYahooConsumer(String subscribe, Consumer<PricingData> consumer) throws URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        var uri = new URI("wss://streamer.finance.yahoo.com/?version=2");
        var countDownLatch = new CountDownLatch(1);
        var httpClient = HttpClient.newHttpClient();
        httpClient.newWebSocketBuilder()
        .buildAsync(uri, new WebSocket.Listener() {
            @Override
            public void onOpen(WebSocket webSocket) {
                logger.info("Connection open!");
                webSocket.sendText(subscribe, true);
                webSocket.request(1);
            }

            @Override
            public void onError(WebSocket webSocket, Throwable error) {
                logger.error("Error on WebSocket connection!", error);
                System.exit(-1);
            };

            @Override
            public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
                logger.info("Binary data received! last={}", last);
                webSocket.request(1);
                return null;
            }

            @Override
            public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
                try {
                    var data = new String(message.array(), "UTF-8");
                    logger.info("Ping received! {}", data);
                    webSocket.request(1);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return null;
            }

            @Override
            public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
                try {
                    var data = new String(message.array(), "UTF-8");
                    logger.info("Pong received! {}", data);
                    webSocket.request(1);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return null;
            }

            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                logger.info("Text data received! last={} data={}", last, data);
                try {
                    var tree = objectMapper.readTree(data.toString());
                    logger.info("Parsed! tree={}", tree);
                    var message = tree.get("message").asText();
                    var binaryMessage = Base64.getDecoder().decode(message);

                    var pData = PricingData.parseFrom(binaryMessage);
                    logger.info("Parsed! pData={}", pData);
                    consumer.accept(pData);
                } catch (JsonProcessingException jpe) {
                    jpe.printStackTrace();
                } catch (InvalidProtocolBufferException ipbe) {
                    ipbe.printStackTrace();
                }
                webSocket.request(1);
                return null;
            }

            @Override
            public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                logger.warn("Connection closed! statusCode={}, reason={}", statusCode, reason);
                return null;
            }
        });
        return countDownLatch;
    }
    public static void main(String[] args) throws URISyntaxException {
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
                var countDownLatch = startYahooConsumer(System.getenv("SUBSCRIBE_MESSAGE"), data -> {
                    try {
                        var jsonData = JsonFormat.printer().print(data);JsonFormat.printer().print(data);
                        logger.info("Sending data={}", jsonData);
                        var record = new ProducerRecord<String, String>("currence.exchange.prices", 
                                                                        data.getId(), 
                                                                        jsonData);
                        var metadata = producer.send(record).get();
                        logger.info("Record sent to partition {} with offset {}", metadata.partition(), metadata.offset());
                    } catch (InterruptedException | ExecutionException | InvalidProtocolBufferException e) {
                        running.set(false);
                    }
                });
                countDownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Done!");

        
    }
}
