#!/bin/sh
set -e
DIR=$(dirname "$0")
cd $DIR &&
cmd="java -cp bin b.b $*" &&
echo \> $cmd;$cmd
