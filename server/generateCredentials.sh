#!/bin/bash

if [ $# -ne 3 ]; then
    echo "Usage: sh <directory> <username> <organization>"
    exit
fi

directory=$1
username=$2
organization=$3

echo "$directory"
echo "$username"
echo "$organization"

cd $directory
mkdir $username

`keytool -genkeypair -ext san=dns:localhost -storetype PKCS12 -keystore server.p12 -storepass password -alias myalias -keyalg RSA -keysize 4096 -dname "OU=${organization}"`

`keytool -exportcert -rfc -keystore server.p12 -storepass password -alias myalias -file ${username}/${username}.crt`

`openssl pkcs12 -in server.p12 -nocerts -nodes -passin pass:password | openssl rsa -outform PEM -out ${username}/server.private.pem`

`openssl pkcs8 -topk8 -inform PEM -outform DER -in ${username}/server.private.pem -out ${username}/${username}.priv.key -nocrypt`

`rm server.p12 ${username}/server.private.pem`