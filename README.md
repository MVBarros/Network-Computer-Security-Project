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

`openjdk version 1.8.0_232`

`Vagrant 2.2.6`

`Virtualbox 6.0`

### Setup:

All of the modules must be configured with the correct keys. To do so, run the script `root-gen.sh`, which automatically creates and copies the key files to the respective folders. 

They must also have the respective database setups. To do so you need to run the `schema.sql` file in `/src/main/resources/db` of the server, key-server and backup modules. The client has no database and as such no script is needed.

We recomend you run `mvn clean install` on the root directory to install all dependencies for the project and create all of the respective jars. 

The project is configured with hardcoded IPs that are assigned to each VM in the vagrant file.

To create and start the VMs run vagrant up in the folder containing the Vagrantfile. Note that each VM has 512 MBs of RAM and that the script takes a long time to finish.


The VMS that are create are:
* `client`
* `server`
* `tig-firewall`
* `key-server`
* `backup-bd`

To enter a VM do `vagrant ssh <vm name>`.

Before running any servers it is recomended you setup the firewall. Enter the `tig-firewall` VM and run the command `sudo /home/vagrant/proj/iptables/setup.sh`. You might also need to enable packet forwarding. Issue the following command: `sudo sysctl net.ipv4.ip_forward=1`

We hope the names are self explanatory. Each VM has a shared folder of the project directory in the path `/home/vagrant/proj`. In the run folder you will find a script to run each of the Servers (`run_server.sh`, `run_backup.sh`, `run_keys.sh`). 

Run each server in the respective VM in a different terminal window.

To run the client you must enter the client VM. Then go into the `target` directory of the client module and run `java -jar tig-client-1.0.0-SNAPSHOT-jar-with-dependencies.jar` with the options you want to run. The avaliable options are:

*  `-b`                                 Use to list Backup files
*  `-c filename permission target`    Use to change Access Control options of a file
*  `-d filename owner filepath`       Use to Download a file
*  `-e filename owner filepath`       Use to Edit a file
*  `-g filename t_created filepath`   Use to Get a backup file
*  `-l`                                 Use to List all files
*  `-n`                                 Use to register New user
*  `-r filename`                      Use to Remove (delete) a file
*  `-u filename filepath`             Use to Upload a new file


Have fun backing up your files safely!
