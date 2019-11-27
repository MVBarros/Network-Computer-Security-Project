openssl req -x509 -newkey rsa:4096 -keyout ca-less/server.key -out ca-less/cert.pem -days 365 -nodes -subj '/CN=localhost'
