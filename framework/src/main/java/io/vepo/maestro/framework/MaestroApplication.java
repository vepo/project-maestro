package io.vepo.maestro.framework;

import jakarta.enterprise.inject.se.SeContainerInitializer;

public class MaestroApplication {
    public void run() {
        var initializer = SeContainerInitializer.newInstance();
        try (var container = initializer.initialize()) {
            container.select(MaestroConsumer.class)
                     .forEach(consumer -> {
                         System.out.println("Consumer: " + consumer);
                     });
        }
    }

    public void run(Class<?> applicationClass) {
        var initializer = SeContainerInitializer.newInstance();
        try (var container = initializer.disableDiscovery()
                                        .addPackages(true, applicationClass.getPackage())
                                        .initialize()) {
            container.select(MaestroConsumer.class)
                     .forEach(consumer -> {
                         System.out.println("Consumer: " + consumer);
                     });
        }
    }
}
