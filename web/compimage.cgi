#!/bin/sh

. ./cgi2common.sh

echo "Content-Type:image/gif"
echo

#pipe to dot for a gif
$JAVA $CLASSES foldoccmd.BioImage \
	-DREQUEST_METHOD="$REQUEST_METHOD" \
        -DQUERY_STRING="$QUERY_STRING" | rsh cpu0 $DOTCMD
