package dev.vepo.maestro.kafka.manager.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Optional;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_cluster_ssl_credentials")
public class SslCredentials {

    private static final MessageDigest loadMessageDigest() {
        try {
            return MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No SHA-512 found!");
        }
    }

    private static Optional<File> getTemporaryFile(String filename, byte[] data) {
        if (Objects.nonNull(filename)) {
            var hashDigest = loadMessageDigest();
            hashDigest.update(data);
            var hash = hashDigest.digest();
            var hashStringBuilder = new StringBuilder();
            for (byte b : hash) {
                // Convert each byte to a 2-digit hex value
                hashStringBuilder.append(String.format("%02x", b));
            }
            var tempPath = Paths.get("/", "tmp", hashStringBuilder.toString(), filename);
            if (!tempPath.toFile().exists()) {
                tempPath.toFile().getParentFile().mkdirs();
                try (FileOutputStream fos = new FileOutputStream(tempPath.toFile())) {
                    fos.write(data);
                } catch (IOException ioe) {
                    return Optional.empty();
                }
            }
            return Optional.of(tempPath.toFile());
        } else {
            return Optional.empty();
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "keystore")
    private byte[] keystore;

    @Column(name = "keystore_filename")
    private String keystoreFilename;

    @Column(name = "keystore_password")
    private String keystorePassword;

    @Lob
    @Column(name = "truststore")
    private byte[] truststore;

    @Column(name = "truststore_filename")
    private String truststoreFilename;

    @Column(name = "truststore_password")
    private String truststorePassword;

    @Column(name = "key_password")
    private String keyPassword;

    public SslCredentials(byte[] keystore,
            String keystoreFilename,
            String keystorePassword,
            byte[] truststore,
            String truststoreFilename,
            String truststorePassword,
            String keyPassword) {
        this.keystore = keystore;
        this.keystoreFilename = keystoreFilename;
        this.keystorePassword = keystorePassword;
        this.truststore = truststore;
        this.truststoreFilename = truststoreFilename;
        this.truststorePassword = truststorePassword;
        this.keyPassword = keyPassword;
    }

    public SslCredentials() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte[] getKeystore() {
        return keystore;
    }

    public void setKeystore(byte[] keystore) {
        this.keystore = keystore;
    }

    public String getKeystoreFilename() {
        return keystoreFilename;
    }

    public void setKeystoreFilename(String keystoreFilename) {
        this.keystoreFilename = keystoreFilename;
    }

    public String getTruststoreFilename() {
        return truststoreFilename;
    }

    public void setTruststoreFilename(String truststoreFilename) {
        this.truststoreFilename = truststoreFilename;
    }

    public Optional<File> getTemporaryTruststore() {
        return getTemporaryFile(truststoreFilename, truststore);
    }

    public Optional<File> getTemporaryKeystore() {
        return getTemporaryFile(keystoreFilename, keystore);
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }

    public void setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
    }

    public byte[] getTruststore() {
        return truststore;
    }

    public void setTruststore(byte[] truststore) {
        this.truststore = truststore;
    }

    public String getTruststorePassword() {
        return truststorePassword;
    }

    public void setTruststorePassword(String truststorePassword) {
        this.truststorePassword = truststorePassword;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        SslCredentials other = (SslCredentials) obj;
        return Objects.equals(id, other.id);
    }

    @Override
    public String toString() {
        return String.format("SslCredentials [id=%d, keystore=%s, keystoreFilename=%s, keystorePassword=%s, truststore=%s, truststoreFilename=%s, truststorePassword=%s, keyPassword=%s]",
                             id, keystore, keystoreFilename, keystorePassword, truststore, truststoreFilename,
                             truststore, keyPassword);
    }
}
