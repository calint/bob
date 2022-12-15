#!/bin/sh
set -e
DIR=$(dirname "$0")
cd $DIR

IPS=$(cat cluster-ips.txt | sed -r '/^\s*$/d' | sed -r '/^\s*#/d')

IP=$(set -- $IPS && echo $1)
CMD='systemctl restart bob-cluster'
echo $IP: $CMD
ssh root@$IP "$CMD"

for IP in $IPS; do
	CMD='systemctl restart bob'
	echo $IP: $CMD
	ssh -n root@$IP "$CMD" &
done

wait
echo