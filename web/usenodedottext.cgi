#!/bin/sh

. ./cgicommon.sh
. ./usecommon.sh

echo "Content-Type:text/plain"
echo

#pipe stderr to stdout for progress/textual dot
$JAVA $CLASSES foldoccmd.QuickDot $ONTOLOGY $CGIQUERY HCI 2>&1
