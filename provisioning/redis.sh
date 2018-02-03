#!/usr/bin/env bash

set -ex

REDIS_VERSION='4.0.8'

echo "Installing redis version ${REDIS_VERSION}..."

# prerequisites
sudo apt-get install build-essential tcl -y

# install redis
cd /tmp
curl -O http://download.redis.io/releases/redis-${REDIS_VERSION}.tar.gz
tar -xzvf redis-${REDIS_VERSION}.tar.gz
cd redis-${REDIS_VERSION}
make
#make test
sudo make install

# configure redis
sudo mkdir /etc/redis
sudo cp /tmp/redis-${REDIS_VERSION}/redis.conf /etc/redis
sudo sed -i "s/supervised no/supervised systemd/" /etc/redis/redis.conf
sudo sed -i "s/dir \.\//dir \/var\/lib\/redis/" /etc/redis/redis.conf

# configure os settings for redis
# see https://redis.io/topics/admin
echo "vm.overcommit_memory = 1" | sudo tee -a /etc/sysctl.conf
sudo sysctl vm.overcommit_memory=1
sudo sed -i -e '$i \echo never > /sys/kernel/mm/transparent_hugepage/enabled\n' /etc/rc.local

# move redis.service systemd unit file to correct location
sudo mv /tmp/redis.service /etc/systemd/system/redis.service

# create redis user and group
sudo adduser --system --group --no-create-home redis

# create redis working directory and limit access
sudo mkdir /var/lib/redis
sudo chown redis:redis /var/lib/redis
sudo chmod 770 /var/lib/redis

# start redis
sudo systemctl start redis

# enable redis to start at boot
sudo systemctl enable redis

echo "Installed redis version ${REDIS_VERSION}"
