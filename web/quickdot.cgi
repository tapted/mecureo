#!/bin/sh

. ./cgicommon.sh

echo "Content-Type:image/gif"
echo

#pipe to dot for a gif
$JAVA $CLASSES foldoccmd.QuickDot $ONTOLOGY $CGIQUERY | rsh cpu0 $DOTCMD
