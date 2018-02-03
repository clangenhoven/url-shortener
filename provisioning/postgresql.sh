#!/usr/bin/env bash

set -ex

USERNAME='shortly'
PASSWORD='6t]^yBgX?t{8b5k2?(3D'

echo "Installing postgresql..."

# install postgresql
sudo apt-get install postgresql postgresql-contrib -y

# set up for network access
CONFIG_FILE=`sudo -u postgres psql -Aqtc "SHOW config_file;"`
HBA_FILE=`sudo -u postgres psql -Aqtc "SHOW hba_file;"`

# accept remote connections
sudo sed -i "s/#listen_addresses = 'localhost'/listen_addresses = '*'/" ${CONFIG_FILE}

# use password authentication for remote connections
echo "host    all             all              0.0.0.0/0                       md5" | sudo tee -a ${HBA_FILE}

# create application user
sudo -u postgres psql -c "CREATE USER ${USERNAME} WITH PASSWORD '${PASSWORD}';"

# create application db
sudo -u postgres createdb ${USERNAME}

# restart postgresql
sudo systemctl restart postgresql

echo "Installed postgresql"
