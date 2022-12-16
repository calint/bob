#!/bin/sh
set -e
DIR=$(dirname "$0")
cd $DIR

IPS=$(cat cluster-ips.txt | sed -r '/^\s*$/d' | sed -r '/^\s*#/d')

for IP in $IPS; do
	CMD='cd /bob && git add * && git stash && git pull https://github.com/calint/bob && ./build.sh'
	echo $IP: $CMD
	ssh -n root@$IP "$CMD" &
done

wait
echo

for IP in $IPS; do
	echo $IP: scp ...
	scp -q run.cfg.do root@$IP:/bob/run.cfg &
	scp -q cluster.cfg.do root@$IP:/bob/cluster.cfg &
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