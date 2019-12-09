# Network-Computer-Security-Project

## Project for the Network & Computer Security Class

### Group Members:

Carolina Carreira

Daniela Mendes

Miguel Barros

### Requirements:

`OpenSSL 1.1.1`

`sqlite3 3.30.1`

`Apache Maven 3.6.2`

### Setup:

All of the modules must be configured with the correct keys. To do so, run the script `root-gen.sh`, which automatically creates and copies the key files to the respective folders. 

They must also have the respective database setups. To do so you need to run the `schema.sql` file in `/src/main/resources/db` of the server, key-server and backup modules. The client has no database and as such no script is needed.

We recomend you run `mvn clean install` on the root directory to install all dependencies for the project. 

You should then run `mvn compile exec:java` in the key-server, backup and server modules by that order (note that starting them in a different order will mean the system will not run)

To run the client yo must first to `mvn clean install` on the client module. Then go into the `target` directory and run `java -jar tig-client-1.0.0-SNAPSHOT-jar-with-dependencies.jar` with the options you want to run. The avaliable options are:

*  `-b`                                 Use to list Backup files
*  `-c filename permission target`    Use to change Access Control options of a file
*  `-d filename owner filepath`       Use to Download a file
*  `-e filename owner filepath`       Use to Edit a file
*  `-g filename t_created filepath`   Use to Get a backup file
*  `-l`                                 Use to List all files
*  `-n`                                 Use to register New user
*  `-r filename`                      Use to Remove (delete) a file
*  `-u filename filepath`             Use to Upload a new file

