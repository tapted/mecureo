#!/bin/sh

. ./cgicommon.sh
. ./usecommon.sh

echo "Content-Type:image/gif"
echo

#pipe to dot for a gif
$JAVA $CLASSES foldoccmd.QuickDot $ONTOLOGY $CGIQUERY HCI | rsh cpu0 $DOTCMD
