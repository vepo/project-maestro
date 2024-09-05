package io.vepo.maestro.framework.apps;

import java.util.ArrayList;
import java.util.List;

public class DataBuffer {
    private final List<Data> receivedData = new ArrayList<>();

    public void clean() {
        receivedData.clear();
    }

    public int receivedDataSize() {
        return receivedData.size();
    }

    public List<Data> pollReceivedData() {
        var data = new ArrayList<>(receivedData);
        receivedData.clear();
        return data;
    }

    public void offer(Data data) {
        this.receivedData.add(data);
    }
}
