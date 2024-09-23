CREATE TABLE cluster
(
  id   INT PRIMARY KEY,
  name VARCHAR(64) NOT NULL,
  bootstrapServers VARCHAR(256) NOT NULL,
  CONSTRAINT cluster_name_unique UNIQUE (name)
);

CREATE SEQUENCE cluster_id_seq START 1;