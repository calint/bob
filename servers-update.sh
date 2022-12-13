#!/bin/sh
set -e
DIR=$(dirname "$0")
cd $DIR

IPS=$(cat servers-ips.txt | sed -r '/^\s*$/d' | sed -r '/^\s*#/d')

for IP in $IPS; do
	CMD='cd /bob && git pull https://github.com/calint/bob && ./build.sh && systemctl restart bob'
	echo $IP: $CMD
	ssh -n root@$IP $CMD &
	echo
done

wait
