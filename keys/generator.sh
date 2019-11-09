#generate CA key pair
openssl genrsa -out ca.key

#save public key
openssl rsa -in ca.key -pubout > ca_pub.key

#create signing request
openssl req -new -key ca.key -out ca.csr

#self sign
openssl x509 -req -days 365 -in ca.csr -signkey ca.key -out ca.crt

openssl x509 -in ca.crt -out ca.pem -outform PEM

#create database
echo 01 > ca.srl

#generate server key pair
openssl genrsa -out server.key

#save public key
openssl rsa -in server.key -pubout > server_pub.key

#create signing request
openssl req -new -key server.key -out server.csr

#sign with the CA key
openssl x509 -req -days 365 -in server.csr -CA ca.crt -CAkey ca.key -out server.crt

openssl x509 -in server.crt -out server.pem -outform PEM

#convert to format pkcs8
openssl pkcs8 -topk8 -nocrypt -in server.key -out server2.key

mv server2.key server.key
