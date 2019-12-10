apt update && apt upgrade
apt install sqlite3 -y
apt install openjdk-8-jdk openjdk-8-jre -y
sudo update-java-alternatives --set java-1.8.0-openjdk-amd64
apt install maven -y 
cd /home/vagrant/proj
mvn -o clean install
mvn dependency:go-offline
cp -f /home/vagrant/proj/interfaces/interface_backup.yaml /etc/netplan/60-gateway.yaml
netplan apply