#!/bin/sh

. ./cgicommon.sh
. ./usecommon.sh

echo "Content-Type:text/plain"
echo

#pipe stderr to stdout for rdf progress, with HCI flag
$JAVA $CLASSES foldoccmd.QuickRdf $ONTOLOGY $CGIQUERY HCI 2>&1
