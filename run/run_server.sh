java -jar ../server/target/tig-server-1.0.0-RELEASE-jar-with-dependencies.jar 8080 ../server/src/main/resources/db/tigserver \
../server/src/main/resources/certs/server.pem ../server/src/main/resources/certs/server.key ../server/src/main/resources/certs/server_pcks8.key \
../server/src/main/resources/certs/ca.cert 192.168.50.10:8090 192.168.50.9:8091