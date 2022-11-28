#!/bin/sh
set -e
DIR=$(dirname "$0")
cd $DIR

MYSQL_HOST=localhost:3306
MYSQL_DB=testdb
MYSQL_USER=c
MYSQL_PASSWORD=password
MYSQL_NCONS=100
B_NTHREADS=100
PORT=8888

cmd="java -cp bin:lib/mysql-connector-java-5.1.49.jar b.b bapp_jdbc_host $MYSQL_HOST server_port $PORT thread_pool_size $B_NTHREADS bapp_jdbc_db $MYSQL_DB bapp_jdbc_user $MYSQL_USER bapp_jdbc_password $MYSQL_PASSWORD bapp_jdbc_ncons $MYSQL_NCONS"
echo \> $cmd
$cmd
