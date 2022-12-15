#!/bin/sh
set -e
DIR=$(dirname "$0")
cd $DIR

IPS=$(cat servers-ips.txt | sed -r '/^\s*$/d' | sed -r '/^\s*#/d')

for IP in $IPS; do
	CMD='reboot'
	echo $IP: $CMD
	ssh -n root@$IP "$CMD" &
done

wait
echo