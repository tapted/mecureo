#!/bin/bash

echo Content-Type:text/html
echo

ROOTNODE=$(./getroot $QUERY_STRING)

echo "<html>"
echo "<head>"
echo "<TITLE>VlUM</TITLE>"
echo "<link rel=\"stylesheet\" href=\"vlum.css\" type=\"text/css\">"
echo "<SCRIPT LANGUAGE=\"JavaScript\">"
echo        
echo "</script>"
echo "</HEAD>"
echo "<BODY>"
echo "<APPLET MAYSCRIPT NAME=\"squidgeApplet\" archive=\"vlum.jar,swingall.jar\" CODE=\"squidge.class\" WIDTH=\"100%\" HEIGHT=\"100%\">"
echo "  <param name=\"setupUrl\" value=\"declsetup.xml\">"
echo "  <param name=\"userUrl\" value=\"../usenodequickrdf.cgi?"$QUERY_STRING"\">"
echo "  <param name=\"helpUrl\" value=\"help.html\">"
echo "  <param name=\"rlogUrl\" value=\"http://localhost:8080/squidge/rlogServlet\">"
echo "  <param name=\"initialTitle\" value=\""$ROOTNODE"\">"
echo "</APPLET>"
echo "</BODY>"
echo "</HTML>"
