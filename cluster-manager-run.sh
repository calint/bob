#!/bin/sh
# run on cluster manager node by the service manager

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
java -version
cmd="java -cp bin:lib/mysql-connector-java-5.1.49.jar db.Cluster cluster.cfg $MYSQL_DB $MYSQL_USER $MYSQL_PASSWORD"
echo \> $cmd
$cmd
