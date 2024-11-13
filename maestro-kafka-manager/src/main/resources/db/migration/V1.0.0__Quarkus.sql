CREATE TABLE tbl_users (
  id              BIGINT GENERATED BY DEFAULT AS IDENTITY,
  username        VARCHAR(255) UNIQUE,
  email           VARCHAR(255) UNIQUE,
  password        VARCHAR(255),
  role            VARCHAR(64),
  active          BOOLEAN DEFAULT TRUE,
  created_at      TIMESTAMP(6) WITH TIME ZONE,
  updated_at      TIMESTAMP(6) WITH TIME ZONE,

  PRIMARY KEY (id)
);

create table tbl_cluster_ssl_credentials (
  id                  BIGINT GENERATED BY DEFAULT AS IDENTITY,
  truststore          OID,
  truststore_password VARCHAR(255),
  keystore            OID,
  keystore_password   VARCHAR(255),
  key_password        OID,
  PRIMARY KEY (id)
);

CREATE TABLE tbl_clusters (
  id                        BIGINT GENERATED BY DEFAULT AS IDENTITY,
  name                      VARCHAR(255) UNIQUE,
  bootstrap_servers         VARCHAR(255) UNIQUE,
  protocol                  VARCHAR(255) CHECK (protocol in ('SSL','PLAINTEXT')),
  created_at                TIMESTAMP(6) WITH TIME ZONE,
  updated_at                TIMESTAMP(6) WITH TIME ZONE,
  access_ssl_credentials_id BIGINT UNIQUE,

  PRIMARY KEY (id),
  CONSTRAINT tbl_clusters_access_ssl_credentials_id_fk FOREIGN KEY (access_ssl_credentials_id) REFERENCES tbl_cluster_ssl_credentials
);

INSERT INTO tbl_clusters (name, bootstrap_servers, protocol, access_ssl_credentials_id, created_at, updated_at) VALUES ('Main Cluster', 'kafka-0:9092, kafka-1:9094, kafka-2:9096', null, 'PLAINTEXT', NOW(), NOW());
INSERT INTO tbl_users (username, email, password, role, created_at, updated_at) VALUES ('admin', 'admin@maestro.dev', '$2a$10$JQjLXQ.PBxeqfl2XiF/Voe2x33E4SpI4ln5qF2FzR1HojEQho5ilC', 'ADMIN', NOW(), NOW());