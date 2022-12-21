#!/bin/sh
set -e
DIR=$(dirname "$0")
cd $DIR

if [ -z "$1" ]; then echo "First argument not valid. Specify target."; exit 1; fi
TRGT=$1

IPS=$(cat cluster-ips.txt.$TRGT | sed -r '/^\s*$/d' | sed -r '/^\s*#/d')

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