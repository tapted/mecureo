#!/bin/sh

. ./cgicommon.sh

#use the text listing of nodes [loads faster]
ONTOLOGY=$ROOT"txt/usenodes"

echo "Content-Type:text/html"
echo
$JAVA $CLASSES foldoccmd.NodeSearch $ONTOLOGY $CGIQUERY
