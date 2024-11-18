#!/bin/bash
## Reference: https://medium.com/jinternals/kafka-ssl-setup-with-self-signed-certificate-part-1-c2679a57e16c
cd scripts/docker/kafka/security
set -o nounset \
    -o errexit \
    -o verbose \
    -o xtrace

find . -name '*.csr'   -exec rm -- '{}' +
find . -name '*.cst'   -exec rm -- '{}' +
find . -name '*.pem'   -exec rm -- '{}' +
find . -name '*.key'   -exec rm -- '{}' +
find . -name '*.req'   -exec rm -- '{}' +
find . -name '*.srl'   -exec rm -- '{}' +
find . -name '*.jks'   -exec rm -- '{}' +
find . -name '*_creds' -exec rm -- '{}' +

# Generate CA key
openssl req -new -x509 -keyout ca-cert.key -out ca-cert.crt -days 365 -subj '/CN=maestro.io/OU=Maestro/O=Maestro' -passin pass:password -passout pass:password

# Kafkacat
openssl genrsa -des3 -passout "pass:password" -out kafkacat.client.key 1024
openssl req -passin "pass:password" -passout "pass:password" -key kafkacat.client.key -new -out kafkacat.client.req -subj '/CN=kafka.maestro.io/OU=Mestro/O=Maestro'
openssl x509 -req -CA ca-cert.crt -CAkey ca-cert.key -in kafkacat.client.req -out kafkacat-ca1-signed.pem -days 9999 -CAcreateserial -passin "pass:password"



for i in broker-tls-0 broker-tls-1 broker-tls-2 producer consumer schema-registry
do
    echo $i
    mkdir -p ./$i
    # Create keystores
    keytool -genkey -noprompt \
                 -alias $i \
                 -dname "CN=kafka-$i, OU=Maestro, O=Maestro" \
                 -keystore ./$i/kafka.$i.keystore.jks \
                 -keyalg RSA \
                 -storepass password \
                 -storetype JKS \
                 -keypass password

    # Create CSR, sign the key and import back into keystore
    keytool -keystore ./$i/kafka.$i.keystore.jks -alias $i -certreq -file $i.csr -storepass password -keypass password -noprompt

    openssl x509 -req -CA ca-cert.crt -CAkey ca-cert.key -in $i.csr -out $i-ca1-signed.crt -days 9999 -CAcreateserial -passin pass:password

    keytool -keystore ./$i/kafka.$i.keystore.jks -alias CARoot -import -file ca-cert.crt -storepass password -keypass password -noprompt

    keytool -keystore ./$i/kafka.$i.keystore.jks -alias $i -import -file $i-ca1-signed.crt -storepass password -keypass password -noprompt

    # Create truststore and import the CA cert.
    keytool -keystore ./$i/kafka.$i.truststore.jks -alias CARoot -import -file ca-cert.crt -storepass password -storetype JKS -keypass password -noprompt

#   echo "password" > ./$i/${i}_sslkey_creds
#   echo "password" > ./$i/${i}_keystore_creds
#   echo "password" > ./$i/${i}_truststore_creds
done


