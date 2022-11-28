#!/bin/sh
set -e
DIR=$(dirname "$0")
cd $DIR
rm -rf bin
mkdir bin
find src -type f | grep '\.java$' > sources.txt
find src -type f | grep -v '\.java$' > resources.txt
javac -verbose -d bin @sources.txt

cat resources.txt | while read f; do
    CMD="cp $f bin/${f#*/}"
    echo $CMD
    $CMD
done
rm sources.txt
rm resources.txt
