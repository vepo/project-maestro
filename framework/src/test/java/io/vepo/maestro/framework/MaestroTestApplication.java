package io.vepo.maestro.framework;

import java.util.ArrayList;
import java.util.List;

@MaestroConsumer
public class MaestroTestApplication {
    private static final List<Data> receivedData = new ArrayList<>();

    public static void clean() {
        receivedData.clear();
    }

    public static int receivedDataSize() {
        return receivedData.size();
    }

    public static List<Data> pollReceivedData() {
        var data = new ArrayList<>(receivedData);
        receivedData.clear();
        return data;
    }

    public void consume(Data data) {
        System.out.println(data);
        receivedData.add(data);
    }
}
