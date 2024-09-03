package io.vepo.maestro.framework;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@MaestroConsumer
public class MaestroTestApplication {
    public void consume(Data data) {
        System.out.println(data);
    }
}
