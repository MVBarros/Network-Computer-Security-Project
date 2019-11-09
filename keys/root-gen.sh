#cleanup
rm ca.key ca.cert server.key server.pem

#generate ca key
openssl genrsa -out root-ca/ca.key 4096

#convert to Java format
openssl pkcs8 -topk8 -in root-ca/ca.key -nocrypt -out root-ca/ca2.key
mv root-ca/ca2.key root-ca/ca.key

#self sign certificate
openssl req -new -x509 -key root-ca/ca.key -sha256 -subj "/C=PT/ST=LX/O=TIG" -days 365 -out root-ca/ca.cert
#generate server key
openssl genrsa -out root-ca/server.key

#convert to Java format
openssl pkcs8 -topk8 -in root-ca/server.key -nocrypt -out root-ca/server2.key
mv root-ca/server2.key root-ca/server.key

#generate server signing request
openssl req -new -key root-ca/server.key -out root-ca/server.csr -config certificate.conf

#sign server key with ca key
openssl x509 -req -in root-ca/server.csr -CA root-ca/ca.cert -CAkey root-ca/ca.key -CAcreateserial -out root-ca/server.pem -days 365 -sha256 -extfile certificate.conf -extensions req_ext

#delete signing request
#rm server.csr
