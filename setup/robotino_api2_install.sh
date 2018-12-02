#!/bin/bash
wget -qO - http://packages.openrobotino.org/keyFile | sudo apt-key add -
echo "deb http://packages.openrobotino.org/xenial xenial main" > /etc/apt/sources.list.d/openrobotino.list
apt-get update
apt-get upgrade
apt-get install -y robotino-api2
