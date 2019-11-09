#generate ca key
openssl genrsa -out ca.key 

openssl pkcs8 -topk8 -in ca.key -nocrypt -out ca2.key 

mv ca2.key ca.key

#self sign ca key
openssl req -new -x509 -key ca.key -out ca.crt

#generate server key
openssl genrsa -out server.key

openssl pkcs8 -topk8 -in server.key -nocrypt -out server2.key 

mv server2.key server.key

#generate server signing request
openssl req -new -key server.key -out server.csr

#sign server key with ca key
openssl x509 -req -in server.csr -CA ca.crt -CAkey ca.key -CAcreateserial -out server.crt

rm server.csr

mv ca.crt ca.pem

mv server.crt server.pem
