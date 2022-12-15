#!/bin/sh
set -e

HOST=http://localhost:8888

CMD="slowhttptest -gotest1 -l600 -c 50000 -X -r 10000 -w 512 -y 1024 -n 5 -z 32 -k 3 -u $HOST/b/test/t4 -p 3"
echo $CMD
$CMD

# slow post
CMD="slowhttptest -XBgotest2 -ji=1 -ftext/plain -i10 -c50000 -r50000 -l600 -u$HOST/b/test/t4"
echo $CMD
$CMD


