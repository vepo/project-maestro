package io.vepo.maestro.framework.apps.app2;

import io.vepo.maestro.framework.annotations.MaestroConsumer;
import io.vepo.maestro.framework.annotations.Topic;
import io.vepo.maestro.framework.apps.Data;
import io.vepo.maestro.framework.apps.DataBuffer;

/**
 * Application2:
 * 
 * It should consume messages from the topic "topic-consume" and store them in a buffer.
 */
@MaestroConsumer
public class Application2 {
    public static final DataBuffer buffer = new DataBuffer();

    @Topic("topic-consume")
    public void consume(Data data) {
        buffer.offer(data);
    }
}
