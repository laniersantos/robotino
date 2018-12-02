#!/bin/bash
add-apt-repository ppa:webupd8team/java
apt-get update
apt install -y oracle-java8-installer
export JAVA_HOME="/usr/lib/jvm/java-8-oracle/"

