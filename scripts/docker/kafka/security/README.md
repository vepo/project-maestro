# How to generate security files

https://code.kx.com/insights/1.3/extras/kafka-tls-setup.html

```bash
## (1) Generate Keystore
keytool -genkey -keyalg RSA -keystore keystore.jks -alias kafka-tls -validity 36500
### (2) Generate self-signed certificate
openssl req -new -x509 -keyout ca-key.pem -out ca-cert.pem -days 36500
### (3) Generate the truststore with the certificate (2)
keytool -import -keystore truststore.jks -alias CARoot -file ca-cert.pem -trustcacerts
### (4) Generate a Sign Request using the KeyStore
keytool -certreq -keystore keystore.jks -alias kafka-tls -file unsigned.pem -trustcacerts
### (5) Sign the Sign Request (4) with the certificate (2)
openssl x509 -req -CA ca-cert.pem -CAkey ca-key.pem -in unsigned.pem -out signed.pem -days 36500 -CAcreateserial
### (6) Store the certificate as trusted in Keystore (1)
keytool -import -keystore keystore.jks -alias CARoot -file ca-cert.pem -trustcacerts
### (7) Store the Signed Request as trusted in Keystore (1)
keytool -import -keystore keystore.jks -alias kafka-tls -file signed.pem -trustcacerts
### (8) Export the certificate from Keystore (1)
keytool -exportcert -alias kafka-tls -keystore keystore.jks -rfc -file cert.pem
### (9) Convert Keystore JKS into PKCS12
keytool -importkeystore \
        -srckeystore keystore.jks \
        -destkeystore keystore.p12 \
        -deststoretype PKCS12 \
        -srcalias kafka-tls
### (10) Export the Keystore private key
openssl pkcs12 -in keystore.p12 -nocerts -nodes -out key.pem
### Cleanup unused files
rm ca-key.pem unsigned.pem signed.pem keystore.p12
```
