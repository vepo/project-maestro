package dev.vepo.maestro.kafka.manager.model;

import java.util.Objects;

import io.undertow.server.session.SslSessionConfig;
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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "keystore")
    private byte[] keystore;

    @Column(name = "keystore_password")
    private String keystorePassword;

    @Lob
    @Column(name = "truststore")
    private byte[] truststore;

    @Column(name = "truststore_password")
    private String truststorePassword;

    @Lob
    @Column(name = "key_password")
    private String keyPassword;

    public SslCredentials(byte[] keystore, String keystorePassword, byte[] truststore, String truststorePassword, String keyPassword) {
        this.keystore = keystore;
        this.keystorePassword = keystorePassword;
        this.truststore = truststore;
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
        return String.format("SslCredentials [id=%d, keystore=%s, keystorePassword=%s, truststore=%s, truststorePassword=%s, keyPassword=%s]",
                             id, keystore, keystorePassword, truststore, truststore, keyPassword);
    }
}
