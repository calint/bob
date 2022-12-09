#!/bin/sh
set -e

NREQ=100000
CON=1000
URL=http://localhost:8888

ab -n $NREQ -c $CON $URL/ > get-from-cache.txt
ab -n $NREQ -c $CON $URL/qa/files/far_side_dog_ok.jpg > get-medium-file.txt
ab -n $NREQ -c $CON $URL/b/test/t1 > dynamic-hello-world.txt
