#cleanup
rm ca.key ca.cert server.key certificate.conf server.pem

#generate ca key
openssl genrsa -out ca.key 4096

#convert to Java format
openssl pkcs8 -topk8 -in ca.key -nocrypt -out ca2.key
mv ca2.key ca.key

#self sign certificate
openssl req -new -x509 -key ca.key -sha256 -subj "/C=PT/ST=LX/O=TIG" -days 365 -out ca.cert
#generate server key
openssl genrsa -out server.key

#convert to Java format
openssl pkcs8 -topk8 -in server.key -nocrypt -out server2.key
mv server2.key server.key

#generate server signing request
openssl req -new -key server.key -out server.csr -config certificate.conf

#sign server key with ca key
openssl x509 -req -in server.csr -CA ca.cert -CAkey ca.key -CAcreateserial -out server.pem -days 365 -sha256 -extfile certificate.conf -extensions req_ext

#delete signing request
#rm server.csr
