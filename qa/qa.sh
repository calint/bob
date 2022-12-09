#!/bin/sh
set -e

. ./qa.cfg

echo Host: $QA_BASE_URL

URI_FILES="qa/files"
DIR_FILES=files
DIR_CMP=cmp

#--------------------------------------------------------
TEST_NAME="get small file: "
FILE=x.css
echo -n $TEST_NAME
curl -s $QA_BASE_URL/$URI_FILES/$FILE > res
if ! cmp -s $DIR_FILES/$FILE res; then exit 1; fi
echo " ok"
#--------------------------------------------------------
TEST_NAME="get medium file: "
FILE=far_side_dog_ok.jpg
echo -n $TEST_NAME
curl -s $QA_BASE_URL/$URI_FILES/$FILE > res
if ! cmp -s $DIR_FILES/$FILE res; then exit 1; fi
echo " ok"
#--------------------------------------------------------
TEST_NAME="get ranged from cache (range 1-): "
FILE=x.js
echo -n $TEST_NAME
curl -s -r 1- $QA_BASE_URL/$URI_FILES/$FILE > res
if ! cmp -s $DIR_CMP/1 res; then exit 1; fi
echo " ok"
#--------------------------------------------------------
TEST_NAME="get ranged from cache (range -5): "
FILE=x.js
echo -n $TEST_NAME
curl -s -r -5 $QA_BASE_URL/$URI_FILES/$FILE > res
if ! cmp -s $DIR_CMP/2 res; then exit 1; fi
echo " ok"
#--------------------------------------------------------
TEST_NAME="get ranged from cache (range 1-5): "
FILE=x.js
echo -n $TEST_NAME
curl -s -r 1-5 $QA_BASE_URL/$URI_FILES/$FILE > res
if ! cmp -s $DIR_CMP/3 res; then exit 1; fi
echo " ok"
#--------------------------------------------------------
TEST_NAME="get ranged (range 1-): "
FILE=far_side_dog_ok.jpg
echo -n $TEST_NAME
curl -s -r 1- $QA_BASE_URL/$URI_FILES/$FILE > res
if ! cmp -s $DIR_CMP/4 res; then exit 1; fi
echo " ok"
#--------------------------------------------------------
TEST_NAME="get ranged (range -10): "
FILE=far_side_dog_ok.jpg
echo -n $TEST_NAME
curl -s -r -10 $QA_BASE_URL/$URI_FILES/$FILE > res
if ! cmp -s $DIR_CMP/5 res; then exit 1; fi
echo " ok"
#--------------------------------------------------------
TEST_NAME="get ranged (range 1-10): "
FILE=far_side_dog_ok.jpg
echo -n $TEST_NAME
curl -s -r 1-10 $QA_BASE_URL/$URI_FILES/$FILE > res
if ! cmp -s $DIR_CMP/6 res; then exit 1; fi
echo " ok"
#--------------------------------------------------------
TEST_NAME="chained requests (static content): "
echo -n $TEST_NAME
echo -n $'GET /qa/files/x.css HTTP/1.1\r\n\r\nGET /qa/files/x.css HTTP/1.1\r\nConnection: close\r\n\r\n' | nc $QA_HOST $QA_PORT > res
if ! cmp -s $DIR_CMP/7 res; then exit 1; fi
echo " ok"
#--------------------------------------------------------
#TEST_NAME="chained requests (page) check hang: "
#echo -n $TEST_NAME
#echo -n $'GET /b/test/t1 HTTP/1.1\r\n\r\nGET /b/test/t1 HTTP/1.1\r\nConnection: close\r\n\r\n' | nc localhost 8888 > res
#echo " ok"
#--------------------------------------------------------
TEST_NAME="large oschunked reply hang: "
echo -n $TEST_NAME
curl -s $QA_BASE_URL/b/test/t2 > res
SIZE=$(wc -c < res)
if [ $SIZE != 33554592 ]; then exit 1; fi
echo " ok"
#--------------------------------------------------------
TEST_NAME="page 'hello world': "
echo -n $TEST_NAME
curl -s $QA_BASE_URL/b/test/t1 > res
if ! cmp -s $DIR_CMP/8 res; then exit 1; fi
echo " ok"
#--------------------------------------------------------
TEST_NAME="xwriter elements: "
echo -n $TEST_NAME
curl -s $QA_BASE_URL/b/test/t3 > res
if ! cmp -s $DIR_CMP/9 res; then exit 1; fi
echo " ok"
#--------------------------------------------------------
TEST_NAME="page 'hello world statefull': "
echo -n $TEST_NAME
curl -s $QA_BASE_URL/b/test/t4 > res
if ! cmp -s $DIR_CMP/10 res; then exit 1; fi
echo " ok"
#--------------------------------------------------------
rm res
