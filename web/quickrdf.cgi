#!/bin/sh

. ./cgicommon.sh

echo "Content-Type:text/xml"
echo

#show only stdout for XML/RDF
$JAVA $CLASSES foldoccmd.QuickRdf $ONTOLOGY $CGIQUERY
