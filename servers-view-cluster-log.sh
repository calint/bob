#!/bin/sh
set -e
DIR=$(dirname "$0")
cd $DIR

IPS=$(cat servers-ips.txt | sed -r '/^\s*$/d' | sed -r '/^\s*#/d')

IP=$(set -- $IPS && echo $1)
CMD='journalctl -u bob-cluster -f'
echo $IP: $CMD
ssh root@$IP "$CMD"
