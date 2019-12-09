apt update && apt upgrade
apt install sqlite3 -y
apt install maven -y 
cp -f /home/vagrant/proj/interfaces/interface_server.yaml /etc/netplan/60-gateway.yaml
netplan apply