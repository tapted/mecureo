#!/bin/bash

echo Content-Type:text/html
echo

echo "<html>"
echo "<FRAMESET COLS=\"350,*\" BORDER=0 FRAMEBORDER=0>"
echo "   <FRAME SRC=\"vlum/vlumframe.cgi?"$QUERY_STRING"\" NAME=vlum NORESIZE SCROLLING=no MARGINWIDTH=0 MARGINHEIGHT=0 FRAMEBORDER=0 border=0>"
echo "   <FRAME SRC=\"http://foldoc.doc.ic.ac.uk/foldoc/foldoc.cgi?query="$(./plusroot $QUERY_STRING)"\" NAME=topicframe NORESIZE MARGINWIDTH=5 MARGINHEIGHT=5 FRAMEBORDER=0 border=0>"
echo "</FRAMESET>"
echo "</HTML>"
