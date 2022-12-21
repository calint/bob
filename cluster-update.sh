#!/bin/sh
set -e
DIR=$(dirname "$0")
cd $DIR

if [ -z "$1" ]; then echo "First argument not valid. Specify target."; exit 1; fi
TRGT=$1

IPS=$(cat cluster-ips.txt.$TRGT | sed -r '/^\s*$/d' | sed -r '/^\s*#/d')

for IP in $IPS; do
	CMD='cd /bob && git fetch && git reset --hard HEAD && git merge '@{u}' && ./build.sh'

	echo $IP: $CMD
	ssh -n root@$IP "$CMD" &
done

wait
echo

for IP in $IPS; do
	scp run.cfg.do root@$IP:/bob/run.cfg &
	scp cluster.cfg.do root@$IP:/bob/cluster.cfg &
done

wait

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