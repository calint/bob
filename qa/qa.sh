#!/bin/sh
set -e

. ./qa.cfg

echo Host: $QA_HOST

URI_FILES="qa/files"
DIR_FILES=files

#--------------------------------------------------------
TEST_NAME="get small file: "
FILE=x.css
echo -n $TEST_NAME
curl -s $QA_HOST/$URI_FILES/$FILE > res
if ! cmp -s $DIR_FILES/$FILE res; then exit 1; fi
echo " ok"
#--------------------------------------------------------
TEST_NAME="get medium file: "
FILE=far_side_dog_ok.jpg
echo -n $TEST_NAME
curl -s $QA_HOST/$URI_FILES/$FILE > res
if ! cmp -s $DIR_FILES/$FILE res; then exit 1; fi
echo " ok"
#--------------------------------------------------------
