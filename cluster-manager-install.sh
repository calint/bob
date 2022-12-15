#!/bin/sh
set -e
DIR=$(dirname "$0")
cd $DIR

IPS=$(cat cluster-ips.txt | sed -r '/^\s*$/d' | sed -r '/^\s*#/d')
IP=$(set -- $IPS && echo $1)

scp cluster-manager-on-node-install.sh root@$IP:/bob/
ssh root@$IP "sh /bob/cluster-manager-on-node-install.sh"
