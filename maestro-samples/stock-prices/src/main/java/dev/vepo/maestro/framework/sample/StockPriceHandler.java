package dev.vepo.maestro.framework.sample;

import java.time.Instant;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.eclipse.jnosql.mapping.document.DocumentTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vepo.maestro.framework.Metadata;
import io.vepo.maestro.framework.annotations.MaestroConsumer;
import io.vepo.maestro.framework.annotations.Topic;
import io.vepo.maestro.framework.serializers.JsonDeserializer;
import jakarta.inject.Inject;

@MaestroConsumer(keyDeserializer = StringDeserializer.class, valueDeserializer = JsonDeserializer.class)
public class StockPriceHandler {

    private static final Logger logger = LoggerFactory.getLogger(StockPriceHandler.class.getName());

    @Inject
    DocumentTemplate template;

    @Topic("stock-prices")
    public void consumeStockPrice(StockPrice stockPrice, Metadata metadata) {
        logger.info("Consuming stock price: {}", stockPrice);
        logger.info("Metadata: {}", metadata);
        logger.info("Saving stock price into database! {}", template);
        template.insert(new Quote(stockPrice.id(), stockPrice.price(), Instant.ofEpochMilli(metadata.timestamp())));
    }
}
