#!/bin/sh
#command to run java - basser ugrad needs to rsh, otherwise just `java' should work
#JAVA="java"
JAVA="rsh congo4 /usr/local/bin/java"

#when not rshing this will work
#ROOT="./"
ROOT="/usr/cs3/tapted/lib/html/mecureo/"


#location of mecureo.jar, or the classes directory
#because of the rsh this has to be absolute on ugrad
CLASSES="-classpath "$ROOT"mecureo.jar"
#use this for local
#CLASSES="-classpath ./mecureo.jar"

#which ontology graph to load. The matchedont is more accurate.
#submatchedont is denser but some links don't make a lot of sense
ONTOLOGY=$ROOT"fdg/matchedont.fdg"
#ONTOLOGY=$ROOT"fdg/submatchedont.fdg"
#ONTOLOGY=$ROOT"fdg/usability.fdg"
#use the text listing of nodes [for nodesearch - loads faster]
#ONTOLOGY=$ROOT"txt/usenodes"

#this converts a cgi query string into the format required for the command
#line based interaction
CGIQUERY="`"$ROOT"bin/spacify $QUERY_STRING`"

#the command for dot [AT&T Graphviz] - output is piped to this
DOTCMD=$ROOT"bin/dot -Tgif"

echo "Content-Type:image/gif"
echo

#pipe to dot for a gif
$JAVA $CLASSES foldoccmd.QuickDot $ONTOLOGY $CGIQUERY | $DOTCMD
