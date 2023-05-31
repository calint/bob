#!/bin/sh
# run on the node by service manager

set -e
DIR=$(dirname "$0")
cd $DIR

CFG=$1
CFG=${CFG:=run.cfg}
echo config: $CFG

cat $CFG
. ./$CFG
echo
java -version
echo

cmd="java -cp bin:lib/mysql-connector-java-5.1.49.jar b.b \
	server_port $B_PORT \
	thread_pool_size $B_NTHREADS \
	bapp_class bob.app.Application;zen.Application \
	bapp_cluster_mode $CLUSTER_MODE \
	bapp_cluster_ip $CLUSTER_IP \
	bapp_cluster_port $CLUSTER_PORT \
	bapp_jdbc_host $MYSQL_HOST \
	bapp_jdbc_db $MYSQL_DB \
	bapp_jdbc_user $MYSQL_USER \
	bapp_jdbc_password $MYSQL_PASSWORD \
	bapp_jdbc_ncons $MYSQL_NCONS"
echo \> $cmd
$cmd
