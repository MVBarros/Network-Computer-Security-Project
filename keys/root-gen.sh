#cleanup
rm -rf root-ca
mkdir root-ca
mkdir root-ca/ca
mkdir root-ca/server
mkdir root-ca/client

#generate ca key
openssl genrsa -out root-ca/ca/ca.key 4096

#convert to grpc format
openssl pkcs8 -topk8 -in root-ca/ca/ca.key -nocrypt -out root-ca/ca/ca2.key
mv root-ca/ca/ca2.key root-ca/ca/ca.key

#self sign certificate
openssl req -new -x509 -key root-ca/ca/ca.key -sha256 -subj "/C=PT/ST=LX/O=TIG" -days 365 -out root-ca/ca/ca.cert

#generate server key
openssl genrsa -out root-ca/server/server.key

#convert to grpc format
openssl pkcs8 -topk8 -in root-ca/server/server.key -nocrypt -out root-ca/server/server2.key
mv root-ca/server/server2.key root-ca/server/server.key

#generate server signing request
openssl req -new -key root-ca/server/server.key -out root-ca/server/server.csr -config certificate.conf

#sign server key with ca key
openssl x509 -req -in root-ca/server/server.csr -CA root-ca/ca/ca.cert -CAkey root-ca/ca/ca.key -CAcreateserial -out root-ca/server/server.pem -days 365 -sha256 -extfile certificate.conf -extensions req_ext

#convert server private key to java format (diferent from grpc format)
openssl pkcs8 -topk8 -inform PEM -outform DER -in root-ca/server/server.key -nocrypt -out root-ca/server/server_pcks8.key


#generate client key
openssl genrsa -out root-ca/client/client.key

#generate client signing request
openssl req -new -key root-ca/client/client.key -out root-ca/client/client.csr -config certificate.conf

#sign client key with ca key
openssl x509 -req -in root-ca/client/client.csr -CA root-ca/ca/ca.cert -CAkey root-ca/ca/ca.key -CAcreateserial -out root-ca/client/client.pem -days 365 -sha256 -extfile certificate.conf -extensions req_ext

#convert client private key to java format
openssl pkcs8 -topk8 -inform PEM -outform DER -in root-ca/client/client.key -nocrypt -out root-ca/client/client_pkcs8.key
mv root-ca/client/client_pkcs8.key root-ca/client/client.key

#delete csr
rm root-ca/server/server.csr
rm root-ca/client/client.csr