#!/bin/sh

#Set some resource limits [5 minutes max]
ulimit -t 300

#when not rshing this will work
#ROOT="./"
#ROOT="/usr/cs3/tapted/lib/html/mecureo/"
ROOT="/usr/staff/tapted/mecureo/"

#command to run java - basser ugrad needs to rsh, otherwise just `java' should work
#JAVA="java"
#JAVA="rsh congo4 /usr/local/bin/java"
JAVA="nice -19 rsh cpu0 nice -19 /local/usr/bin/java -DROOT="$ROOT

#location of mecureo.jar, or the classes directory
#because of the rsh this has to be absolute on ugrad
CLASSES="-classpath "$ROOT"mecureo.jar"

#which ontology graph to load. The matchedont is more accurate.
#submatchedont is denser but some links don't make a lot of sense
#ONTOLOGY=$ROOT"fdg/detectedont.fdg"
#ONTOLOGY=$ROOT"fdg/matchedont.fdg"
#ONTOLOGY=$ROOT"fdg/submatchedont.fdg"
#ONTOLOGY=$ROOT"fdg/usability.fdg"
#use the text listing of nodes [for nodesearch - loads faster]
#ONTOLOGY=$ROOT"txt/usenodes"

#ontology now got from cgi script
ONTOLOGY=""

#this converts a cgi query string into the format required for the command
#line based interaction
#echo "Content-Type:text/plain"
#echo
#echo "$ROOT/bin/spacify $QUERY_STRING"
#$ROOT/bin/spacify $QUERY_STRING 2>&1
CGIQUERY="`"$ROOT"bin/spacify $QUERY_STRING`"

#the command for dot [AT&T Graphviz] - output is piped to this
DOTCMD=$ROOT"bin/nicedot -Tgif"

#DOT also needs a font path on staff
DOTFONTPATH="/usr/X11R6/lib/X11/fonts/Type1:/usr/X11R6/lib/X11/fonts/latin2/pnm"
export DOTFONTPATH

export ROOT
export JAVA
export CGIQUERY
export DOTCMD
export ONTOLOGY
export CLASSES
