package dev.vepo.maestro.framework.sample.message;

import java.io.FileWriter;
import java.io.IOException;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vepo.maestro.framework.annotations.MaestroConsumer;
import io.vepo.maestro.framework.annotations.Topic;
import jakarta.inject.Inject;

@MaestroConsumer(valueDeserializer = StringDeserializer.class)
public class MessageHandle {

    private static final Logger logger = LoggerFactory.getLogger(MessageHandle.class.getName());

    @Inject
    @ConfigProperty(name = "message.handle.file.path")
    private String filePath;

    @Topic("messages")
    public void handleMessage(String message) {
        logger.info("Message received: {}", message);
        // save into file path
        try (FileWriter writer = new FileWriter(filePath, true)) {
            writer.write(message + System.lineSeparator());
        } catch (IOException e) {
            logger.error("Error writing message to file", e);
        }
    }
}
