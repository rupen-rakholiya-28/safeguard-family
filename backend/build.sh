#!/bin/bash
set -e

cd backend

# Download and extract Maven
if [ ! -d "apache-maven-3.9.6" ]; then
    curl -sL https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz -o /tmp/maven.tar.gz
    tar -xzf /tmp/maven.tar.gz -C /tmp
fi

# Run Maven
/tmp/apache-maven-3.9.6/bin/mvn clean package -DskipTests