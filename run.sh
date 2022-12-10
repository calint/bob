#!/bin/sh
set -e
DIR=$(dirname "$0")
cd $DIR

if [ -f $1 ]; then
	CFG=run.cfg
else
	CFG=$1
fi
echo $CFG
. ./$CFG

cmd="java -cp bin:lib/mysql-connector-java-5.1.49.jar b.b \
	bapp_cluster_ip $CLUSTER_IP \
	bapp_cluster_port $CLUSTER_PORT \
	bapp_jdbc_host $MYSQL_HOST \
	server_port $B_PORT \
	thread_pool_size $B_NTHREADS \
	bapp_jdbc_db $MYSQL_DB \
	bapp_jdbc_user $MYSQL_USER \
	bapp_jdbc_password $MYSQL_PASSWORD \
	bapp_jdbc_ncons $MYSQL_NCONS"
echo \> $cmd
$cmd
