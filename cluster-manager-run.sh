#!/bin/sh
# run on cluster manager node by the service manager

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
cmd="java -cp bin:lib/mysql-connector-java-5.1.49.jar db.Cluster cluster.cfg $MYSQL_DB $MYSQL_USER $MYSQL_PASSWORD"
echo \> $cmd
$cmd
