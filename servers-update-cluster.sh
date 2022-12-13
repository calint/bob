#!/bin/sh
set -e
DIR=$(dirname "$0")
cd $DIR

IPS=$(cat servers-ips.txt | sed -r '/^\s*$/d' | sed -r '/^\s*#/d')

for IP in $IPS; do
	CMD='cd /bob && git pull https://github.com/calint/bob && ./build.sh'
	echo $IP: $CMD
	ssh -n root@$IP $CMD &
done

wait
echo

for IP in $IPS; do
	CMD='systemctl restart bob'
	echo $IP: $CMD
	ssh -n root@$IP $CMD &
done

wait
echo

IP=$(cat servers-ips.txt | sed -r '/^\s*$/d' | sed -r '/^\s*#/d' | head -n1)
CMD='systemctl restart bob-cluster'
echo $IP: $CMD
ssh root@$IP $CMD
echo
