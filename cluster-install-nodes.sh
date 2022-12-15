#!/bin/sh
set -e
DIR=$(dirname "$0")
cd $DIR

IPS=$(cat cluster-install-nodes.cfg | sed -r '/^\s*$/d' | sed -r '/^\s*#/d')

for IP in $IPS; do
	scp -o StrictHostKeyChecking=no cluster-on-node-install.sh root@$IP:/
	ssh -n root@$IP "cd / && sh cluster-on-node-install.sh" &
done

wait
echo
