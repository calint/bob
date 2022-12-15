#!/bin/sh
set -e
DIR=$(dirname "$0")
cd $DIR

IPS=$(cat servers-ips.txt | sed -r '/^\s*$/d' | sed -r '/^\s*#/d')

for IP in $IPS; do
	echo $IP
	scp run.cfg.do root@$IP:/bob/run.cfg &
	scp cluster.cfg.do root@$IP:/bob/cluster.cfg &
done

wait
