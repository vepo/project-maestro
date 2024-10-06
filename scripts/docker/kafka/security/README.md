# How to generate security files

https://code.kx.com/insights/1.3/extras/kafka-tls-setup.html

## Kafka Broker

```bash
## (1) Generate Keystore
keytool -genkey -keyalg RSA -dname "cn=kafka-tls-*, ou=Maestro, o=Maestro, c=BR" -keystore broker-keystore.jks -alias kafka-tls -validity 36500 -storepass 123456
### (2) Generate self-signed certificate
openssl req -new -x509 -keyout ca-key.pem -out ca-cert.pem -days 36500 -config openssl.cnf --passout pass:123456
### (3) Generate the truststore with the certificate (2)
keytool -import -keystore broker-truststore.jks -alias CARoot -file ca-cert.pem -trustcacerts --storepass 123456 --noprompt
### (4) Generate a Sign Request using the KeyStore
keytool -certreq -keystore broker-keystore.jks -alias kafka-tls -file unsigned.pem -trustcacerts --storepass 123456
### (5) Sign the Sign Request (4) with the certificate (2)
openssl x509 -req -CA ca-cert.pem -CAkey ca-key.pem -in unsigned.pem -out signed.pem -days 36500 -CAcreateserial --passin pass:123456
### (6) Store the certificate as trusted in Keystore (1)
keytool -import -keystore broker-keystore.jks -alias CARoot -file ca-cert.pem -trustcacerts --storepass 123456 --noprompt
### (7) Store the Signed Request as trusted in Keystore (1)
keytool -import -keystore broker-keystore.jks -alias kafka-tls -file signed.pem -trustcacerts --storepass 123456 --noprompt
### (8) Export the certificate from Keystore (1)
keytool -exportcert -alias kafka-tls -keystore broker-keystore.jks -rfc -file cert.pem --storepass 123456
### (9) Convert Keystore JKS into PKCS12
keytool -importkeystore -srckeystore broker-keystore.jks -destkeystore broker-keystore.p12 -deststoretype PKCS12 -srcalias kafka-tls --srcstorepass 123456 -deststorepass 123456
### (10) Export the Keystore private key
openssl pkcs12 -in broker-keystore.p12 -nocerts -nodes -out broker-key.pem --passin pass:123456
```

## Kafka Producer

```bash
keytool -genkey -keyalg RSA -dname "cn=producer, ou=Maestro, o=Maestro, c=BR"  -keystore producer-keystore.jks -alias producer -validity 36500 -storepass 123456
keytool -certreq -keystore producer-keystore.jks -alias producer -file unsigned.pem -trustcacerts
keytool -import -keystore producer-truststore.jks -alias CARoot -file ca-cert.pem -trustcacerts --storepass 123456 --noprompt
keytool -certreq -keystore producer-keystore.jks -alias producer -file unsigned.pem -trustcacerts --storepass 123456
openssl x509 -req -CA ca-cert.pem -CAkey ca-key.pem -in unsigned.pem -out signed.pem -days 36500 -CAcreateserial --passin pass:123456
keytool -import -keystore producer-keystore.jks -alias CARoot -file ca-cert.pem -trustcacerts --storepass 123456 --noprompt
keytool -import -keystore producer-keystore.jks -alias producer -file signed.pem -trustcacerts --storepass 123456 --noprompt
keytool -exportcert -alias producer -keystore producer-keystore.jks -rfc -file cert.pem --storepass 123456
keytool -importkeystore -srckeystore producer-keystore.jks -destkeystore producer-keystore.p12 -deststoretype PKCS12 -srcalias producer --srcstorepass 123456 -deststorepass 123456
openssl pkcs12 -in producer-keystore.p12 -nocerts -nodes -out producer-key.pem --passin pass:123456
```


## Cleanup

```bash
### Cleanup unused files
rm ca-key.pem unsigned.pem signed.pem broker-keystore.p12
```
