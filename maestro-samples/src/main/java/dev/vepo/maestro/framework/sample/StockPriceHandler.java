package dev.vepo.maestro.framework.sample;

import org.apache.kafka.common.serialization.StringDeserializer;

import io.vepo.maestro.framework.annotations.MaestroConsumer;
import io.vepo.maestro.framework.annotations.Topic;
import io.vepo.maestro.framework.serializers.JsonDeserializer;

@MaestroConsumer(keyDeserializer = StringDeserializer.class, valueDeserializer = JsonDeserializer.class)
public class StockPriceHandler {

    @Topic("stock-price")
    public void consumeStockPrice(StockPrice stockPrice) {
        System.out.println(stockPrice);
    }
}
