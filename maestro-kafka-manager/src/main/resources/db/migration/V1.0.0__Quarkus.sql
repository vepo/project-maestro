CREATE TABLE cluster
(
  id   INT,
  name VARCHAR(64),
  bootstrapServers VARCHAR(256),
);

CREATE SEQUENCE cluster_id_seq START 1;