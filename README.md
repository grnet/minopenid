miopenid
========

Minimum OpenID Connect Server / Client

Note that in order to work you need a functional keystore and client certificates. You can create them as follows.

#### Create server key
    openssl genrsa -out jetty.key 2048
#### Create server certificate
    openssl req -new -x509 -key jetty.key -subj "/C=GR/ST=Attica/L=Athens/O=GRNET S.A./CN=minoath2.grnet.gr" -out jetty.crt
#### Import server certificate into keystore
    keytool -keystore keystore -import -alias jetty -file jetty.crt -trustcacerts

#### Export server key in PKCS12 format
    openssl pkcs12 -inkey jetty.key -in jetty.crt -export -passout pass:12345 -out jetty.pkcs12
#### Import server key into keystore
    keytool -importkeystore -srckeystore jetty.pkcs12 -srcstoretype PKCS12 -srcstorepass "12345" -destkeystore keystore

#### Start certificate series
    echo "02" > ca.srl
 
#### Generate client key

    openssl req -new -newkey rsa:2048 -nodes -out client1.req -keyout client1.key -subj '/C=GR/ST=Attica/CN=minauthclient/EMAILADDRESS=louridas@grnet.gr'
    openssl x509 -CA jetty.crt -CAkey jetty.key -CAserial ca.srl -req -in client1.req -out client1.pem
    openssl pkcs12 -export -in client1.pem -inkey client1.key -out client1.p12 -name "Sample Client Cert"

