#!/bin/sh

. ./cgicommon.sh

echo "Content-Type:text/plain"
echo

#pipe stderr to stdout for text/progress
$JAVA $CLASSES foldoccmd.QuickDot $ONTOLOGY $CGIQUERY 2>&1
