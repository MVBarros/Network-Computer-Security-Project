#!/bin/bash

# server ip -> 192.168.56.10 
# server port -> 8080
# key-server ip -> 192.168.50.10
# key-server port -> 8090
# backup-bd ip -> 192.168.50.9
# backup-bd port -> 8091

#Note: Cannot block input and output due to vagrant using ssh to control the machines
#Note: Cannot know a priori src ports as they are set random by 
#delete former policies
iptables -F

#set default policy to reject

#iptables -P INPUT DROP
iptables -P FORWARD DROP
#iptables -P OUTPUT DROP

#allow server to send requests to key-server and vice versa
iptables -A FORWARD -p tcp -s 192.168.56.10  -d 192.168.50.10 --dport 8090 -j ACCEPT
iptables -A FORWARD -p tcp -s 192.168.50.10 --sport 8090 -d 192.168.56.10  -j ACCEPT


#allow server to send requests to backup-bd and vice versa
iptables -A FORWARD -p tcp -s 192.168.56.10 -d 192.168.50.9 --dport 8091 -j ACCEPT
iptables -A FORWARD -p tcp -s 192.168.50.9 --sport 8091 -d 192.168.56.10 -j ACCEPT