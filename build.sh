#!/bin/sh
set -e
DIR=$(dirname "$0")
cd $DIR

java -version

rm -rf bin
mkdir bin

find src -type f | grep '\.java$' > sources.txt
find src -type f | grep -vE '\.java$|README.md' > resources.txt

javac -Xlint:deprecation -encoding UTF-8 -d bin @sources.txt

echo
echo Resources:
cat resources.txt | while read f; do
    mkdir -p "$(dirname bin/${f#*/})"
    CMD="cp $f bin/${f#*/}"
    echo $CMD
    $CMD
done

rm sources.txt
rm resources.txt
