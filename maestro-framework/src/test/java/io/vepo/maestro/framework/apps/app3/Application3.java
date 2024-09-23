package io.vepo.maestro.framework.apps.app3;

import io.vepo.maestro.framework.annotations.MaestroConsumer;
import io.vepo.maestro.framework.annotations.Topic;
import io.vepo.maestro.framework.apps.Data;
import io.vepo.maestro.framework.apps.DataBuffer;
import io.vepo.maestro.framework.serializers.AvroDeserializer;

@MaestroConsumer(valueDeserializer = AvroDeserializer.class)
public class Application3 {
    public static final DataBuffer buffer = new DataBuffer();

    @Topic("topic-consume")
    public void consume(Data data) {
        buffer.offer(data);
    }
}
