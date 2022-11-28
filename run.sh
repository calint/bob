#!/bin/sh
set -e
DIR=$(dirname "$0")
cd $DIR

cmd="java -cp bin:lib/mysql-connector-java-5.1.49.jar b.b bapp_jdbc_db testdb bapp_jdbc_user c bapp_jdbc_password password bapp_jdbc_ncons 10"
echo \> $cmd;$cmd
