#!/bin/bash -x

. ./cgi2common.sh

echo "Content-Type:text/html"
echo

$JAVA $CLASSES foldoccmd.BioParser \
  -DREQUEST_METHOD="$REQUEST_METHOD" \
  -DQUERY_STRING="$QUERY_STRING" 2>&1
