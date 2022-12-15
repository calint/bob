#!/bin/sh
set -e
DIR=$(dirname "$0")
cd $DIR

IPS=$(cat cluster-ips.txt | sed -r '/^\s*$/d' | sed -r '/^\s*#/d')

for IP in $IPS; do
	ssh -n root@$IP "journalctl -u bob -f" &
done

wait
echo
