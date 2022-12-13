#!/bin/sh
set -e
DIR=$(dirname "$0")
cd $DIR

IPS=$(cat servers-install.cfg | sed -r '/^\s*$/d' | sed -r '/^\s*#/d')

for IP in $IPS; do
	scp server-install.sh root@$IP:/
	ssh -n root@$IP "cd / && sh server-install.sh" &
done

wait
echo
