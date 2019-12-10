Vagrant.configure("2") do |config|
  # The most common configuration options are documented and commented below.
  # For a complete reference, please see the online documentation at
  # https://docs.vagrantup.com.

  # Every Vagrant development environment requires a box. You can search for
  # boxes at https://vagrantcloud.com/search.
  config.vm.box = "base"
  config.ssh.insert_key = false
  config.vbguest.auto_update = true
  config.vm.box_check_update = false
    
  config.vm.define "server" do |server_config|
    server_config.vm.box = "ubuntu/bionic64"
    server_config.vm.hostname = "server"
    server_config.vm.network "private_network", ip: "192.168.56.10"
    server_config.vm.synced_folder ".", "/home/vagrant/proj"    
    server_config.vm.provider "virtualbox" do |vb|
      vb.name = "server"
      opts = ["modifyvm", :id, "--natdnshostresolver1", "on"]
      vb.customize opts
      vb.memory = "512"
    end
    server_config.vm.provision "shell", path: "bootstraps/bootstrap_server.sh"
  end
  config.vm.define "tig-firewall" do |tig_firewall_config|
    tig_firewall_config.vm.box = "ubuntu/bionic64"
    tig_firewall_config.vm.hostname = "tig-firewall"
    tig_firewall_config.vm.network "private_network", ip: "192.168.56.11"
    tig_firewall_config.vm.network "private_network", ip: "192.168.50.11"
    tig_firewall_config.vm.synced_folder ".", "/home/vagrant/proj"    
    tig_firewall_config.vm.provider "virtualbox" do |vb|
      vb.name = "tig-firewall"
      opts = ["modifyvm", :id, "--natdnshostresolver1", "on"]
      vb.customize opts
      vb.memory = "512"
    end
    tig_firewall_config.vm.provision "shell", path: "bootstraps/bootstrap_firewall.sh"
  end
  config.vm.define "key-server" do |key_server_config|
    key_server_config.vm.box = "ubuntu/bionic64"
    key_server_config.vm.hostname = "key-server"
    key_server_config.vm.network "private_network", ip: "192.168.50.10"
    key_server_config.vm.synced_folder ".", "/home/vagrant/proj"    
    key_server_config.vm.provider "virtualbox" do |vb|
      vb.name = "key-server"
      opts = ["modifyvm", :id, "--natdnshostresolver1", "on"]
      vb.customize opts
      vb.memory = "512"
    end
    key_server_config.vm.provision "shell", path: "bootstraps/bootstrap_key_server.sh"
  end
  config.vm.define "backup-bd" do |backup_bd_config|
    backup_bd_config.vm.box = "ubuntu/bionic64"
    backup_bd_config.vm.hostname = "backup-bd"
    backup_bd_config.vm.network "private_network", ip: "192.168.50.9"
    backup_bd_config.vm.synced_folder ".", "/home/vagrant/proj"    
    backup_bd_config.vm.provider "virtualbox" do |vb| 
      vb.name = "backup-bd"
      opts = ["modifyvm", :id, "--natdnshostresolver1", "on"]
      vb.customize opts
      vb.memory = "512"
    end
    backup_bd_config.vm.provision "shell", path: "bootstraps/bootstrap_backup.sh"
  end
  config.vm.define "client" do |client_config|
    client_config.vm.box = "ubuntu/bionic64"
    client_config.vm.hostname = "client"
    client_config.vm.network "private_network", ip: "192.168.56.12"
    client_config.vm.synced_folder ".", "/home/vagrant/proj"    
    client_config.vm.provider "virtualbox" do |vb|
      vb.name = "client"
      opts = ["modifyvm", :id, "--natdnshostresolver1", "on"]
      vb.customize opts
      vb.memory = "512"
    end
    client_config.vm.provision "shell", path: "bootstraps/bootstrap_client.sh"
  end
end
