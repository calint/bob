#!/bin/sh
set -e
DIR=$(dirname "$0")
cd $DIR

if [ -z "$1" ]; then echo "First argument not valid. Specify target."; exit 1; fi
TRGT=$1

IPS=$(cat cluster-install-nodes.cfg.$TRGT | sed -r '/^\s*$/d' | sed -r '/^\s*#/d')

for IP in $IPS; do
	scp -o StrictHostKeyChecking=no cluster-on-node-install.sh root@$IP:/
	ssh -n root@$IP "cd / && sh cluster-on-node-install.sh" &
done

wait
echo
