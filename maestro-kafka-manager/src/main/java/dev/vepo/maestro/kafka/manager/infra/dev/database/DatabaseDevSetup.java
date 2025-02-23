package dev.vepo.maestro.kafka.manager.infra.dev.database;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.sql.DataSource;

import org.postgresql.largeobject.LargeObjectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.resource.spi.IllegalStateException;
import jakarta.transaction.SystemException;

@ApplicationScoped
@IfBuildProfile("dev")
public class DatabaseDevSetup {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseDevSetup.class);
    @Inject
    private DataSource dataSource;

    void onStart(@Observes StartupEvent ev) throws IllegalStateException, SecurityException, SystemException {
        logger.info("Running database initialization for development environment...");
        insertSslCredentials();
    }

    private void insertSslCredentials() throws IllegalStateException, SecurityException, SystemException {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            var lobj = conn.unwrap(org.postgresql.PGConnection.class).getLargeObjectAPI();

            // Load truststore and keystore
            long truststoreOid = uploadFile(lobj, Paths.get("../scripts/docker/kafka/security/producer/kafka.producer.truststore.jks"));
            long keystoreOid = uploadFile(lobj, Paths.get("../scripts/docker/kafka/security/producer/kafka.producer.keystore.jks"));

            // Insert into table
            String sql = """
                         INSERT INTO tbl_cluster_ssl_credentials (
                             truststore, truststore_filename, truststore_password,
                             keystore, keystore_filename, keystore_password,
                             key_password
                         ) VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id
                         """;
            long credentialId;
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, truststoreOid);
                pstmt.setString(2, "kafka.producer.truststore.jks");
                pstmt.setString(3, "password");
                pstmt.setLong(4, keystoreOid);
                pstmt.setString(5, "kafka.producer.keystore.jks");
                pstmt.setString(6, "password");
                pstmt.setString(7, "password");

                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    credentialId = rs.getLong(1);
                    logger.info("Inserted SSL credential with ID: {}", credentialId);
                } else {
                    throw new IllegalStateException("Could not add credentials");
                }
            }

            insertCluster(conn, "Cluster TLS", "kafka-broker-tls-0:9192,kafka-broker-tls-1:9194,kafka-broker-tls-2:9196", credentialId, "SSL");

            // Insert Cluster Plain (No Credentials)
            insertCluster(conn, "Cluster Plain", "kafka-0:9092, kafka-1:9094, kafka-2:9096", null, "PLAINTEXT");

            // Insert Admin User
            insertUser(conn, "admin", "admin@maestro.dev", "$2a$10$JQjLXQ.PBxeqfl2XiF/Voe2x33E4SpI4ln5qF2FzR1HojEQho5ilC", "ADMIN");
            conn.commit();
        } catch (SQLException e) {
            logger.error("Failed to insert SSL credentials", e);
        } catch (FileNotFoundException fnfe) {
            logger.error("Could not find TLS files! Please execute './scripts/generate-security-keys.sh'", fnfe);
        } catch (IOException ioe) {
            logger.error("Error reading files", ioe);
        }
    }

    private static void insertCluster(Connection conn, String name, String bootstrapServers, Long sslCredentialId, String protocol) throws SQLException {
        String sql =
                "INSERT INTO tbl_clusters (name, bootstrap_servers, access_ssl_credentials_id, protocol, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, bootstrapServers);
            if (sslCredentialId != null) {
                pstmt.setLong(3, sslCredentialId);
            } else {
                pstmt.setNull(3, java.sql.Types.BIGINT);
            }
            pstmt.setString(4, protocol);
            pstmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            pstmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            pstmt.executeUpdate();
        }
    }

    private static void insertUser(Connection conn, String username, String email, String passwordHash, String role) throws SQLException {
        String sql = "INSERT INTO tbl_users (username, email, password, role, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, passwordHash);
            pstmt.setString(4, role);
            pstmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            pstmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            pstmt.executeUpdate();
        }
    }

    private long uploadFile(LargeObjectManager lobj, Path filePath) throws SQLException, FileNotFoundException, IOException {
        long oid = lobj.createLO(LargeObjectManager.READ | LargeObjectManager.WRITE);
        try (var obj = lobj.open(oid, LargeObjectManager.WRITE);
                var fis = new FileInputStream(filePath.toFile())) {
            byte[] buf = new byte[2048];
            int bytesRead;
            while ((bytesRead = fis.read(buf)) > 0) {
                obj.write(buf, 0, bytesRead);
            }
        }
        return oid;
    }
}
