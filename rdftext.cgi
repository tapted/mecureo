#!/bin/sh

. ./cgicommon.sh

echo "Content-Type:text/plain"
echo

#pipe stderr to stdout for rdf progress
$JAVA $CLASSES foldoccmd.QuickRdf $ONTOLOGY $CGIQUERY 2>&1
