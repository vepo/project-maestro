package dev.vepo.maestro.kafka.manager.tcp;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Objects;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record TcpCheck(String host, int port) {
    private static final Logger logger = LoggerFactory.getLogger(TcpCheck.class);

    public boolean isListening() {
        logger.info("Checking address: {}", this);
        // Just check if the port is receiving connections
        try (Socket socket = new Socket()) {
            socket.setReuseAddress(true);

            SocketAddress address = new InetSocketAddress(host, port);
            socket.setSoTimeout(500);
            socket.connect(address, 500);
            return socket.isConnected();
        } catch (Exception e) {
            // just ignore it
            logger.warn("Host is unrecheable! {}", this);
            return false;
        }
    }

    public static Stream<TcpCheck> fromKafkaBootstrapServers(String bootstrapServers) {
        if (Objects.nonNull(bootstrapServers) && !bootstrapServers.isEmpty()) {
            return Stream.of(bootstrapServers.split(","))
                         .parallel()
                         .map(address -> address.split(":"))
                         .filter(address -> address.length == 2)
                         .map(address -> new String[] {
                             address[0].trim(),
                             address[1].trim() })
                         .filter(address -> address[1].matches("^\\d+$"))
                         .map(address -> new TcpCheck(address[0], Integer.valueOf(address[1])));
        }
        return Stream.empty();

    }
}
