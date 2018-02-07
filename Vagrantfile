# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure("2") do |config|

  config.vm.box = "ubuntu/xenial64"
  config.vm.box_check_update = true
  config.vm.network "forwarded_port", guest: 5432, host: 5432, host_ip: "127.0.0.1"
  config.vm.network "forwarded_port", guest: 6379, host: 6379, host_ip: "127.0.0.1"
  config.vm.network "forwarded_port", guest: 8080, host: 8080, host_ip: "127.0.0.1"

  config.vm.provider "virtualbox" do |vb|
    vb.memory = "2048"
  end

  config.vm.provision :shell, inline: "sudo apt-get update"
  config.vm.provision :shell, path: "provisioning/openjdk.sh"
  config.vm.provision :file, source: "provisioning/files/redis.service", destination: "/tmp/redis.service"
  config.vm.provision :shell, path: "provisioning/redis.sh"
  config.vm.provision :shell, path: "provisioning/postgresql.sh"
end
