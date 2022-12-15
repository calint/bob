#!/bin/sh
set -e
DIR=$(dirname "$0")
cd $DIR

IPS=$(cat cluster-ips.txt | sed -r '/^\s*$/d' | sed -r '/^\s*#/d')

for IP in $IPS; do
	CMD='echo "drop database if exists testdb;create database testdb;" | mysql'
	echo $IP: $CMD
	ssh -n root@$IP "$CMD" &
done

wait
echo
