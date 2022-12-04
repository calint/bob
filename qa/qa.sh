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
TEST_NAME="chained request: "
echo -n $TEST_NAME
FILE=x.css
echo -n $'GET /qa/files/x.css HTTP/1.1\r\n\r\nGET /qa/files/x.css HTTP/1.1\r\nConnection: close\r\n\r\n' | nc $QA_HOST $QA_PORT > res
if ! cmp -s $DIR_CMP/7 res; then exit 1; fi
echo " ok"

rm res

