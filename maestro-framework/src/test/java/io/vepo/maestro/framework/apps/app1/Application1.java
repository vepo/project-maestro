package io.vepo.maestro.framework.apps.app1;

import io.vepo.maestro.framework.annotations.MaestroConsumer;
import io.vepo.maestro.framework.apps.Data;
import io.vepo.maestro.framework.apps.DataBuffer;

/**
 * Application1:
 * 
 * It should consume messages from the topic "consume-data" and store them in a buffer.
 */
@MaestroConsumer
public class Application1 {
    public static final DataBuffer buffer = new DataBuffer();
    public void consumeData(Data data) {
        buffer.offer(data);
    }
}
