#!/bin/sh
set -e

# slow post
CMD="slowhttptest -XBgoresult -ji=1 -ftext/plain -i10 -c65539 -r65539 -l600 -uhttp://localhost:8888/b/test/t4"
echo $CMD
$CMD


