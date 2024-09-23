package dev.vepo.maestro.framework.sample;

import io.vepo.maestro.framework.MaestroApplication;

public class StockPriceApplication {

    public static void main(String[] args) {
        try (var app = new MaestroApplication()) {
            app.run();
        }
    }
}
