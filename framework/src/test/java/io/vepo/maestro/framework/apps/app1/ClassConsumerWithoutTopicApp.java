package io.vepo.maestro.framework.apps.app1;

import io.vepo.maestro.framework.annotations.MaestroConsumer;
import io.vepo.maestro.framework.apps.Data;
import io.vepo.maestro.framework.apps.DataBuffer;

@MaestroConsumer
public class ClassConsumerWithoutTopicApp {
    public static final DataBuffer buffer = new DataBuffer();
    public void consume(Data data) {
        buffer.offer(data);
    }
}
