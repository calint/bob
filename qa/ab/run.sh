#!/bin/sh
set -e

NREQ=100000
CON=1000
URL=http://localhost:8888

echo bob
ab -n $NREQ -c $CON $URL/ > bob-get-from-cache.txt
ab -n $NREQ -c $CON $URL/qa/files/far_side_dog_ok.jpg > bob-get-medium-file.txt
ab -n $NREQ -c $CON $URL/b/test/t1 > bob-dynamic-hello-world.txt

URL=http://localhost

echo nginx
ab -n $NREQ -c $CON $URL/ > nginx-get-from-cache.txt
ab -n $NREQ -c $CON $URL/qa/files/far_side_dog_ok.jpg > nginx-get-medium-file.txt
#ab -n $NREQ -c $CON $URL/b/test/t1 > dynamic-hello-world.txt
