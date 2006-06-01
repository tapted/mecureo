#!/bin/sh

cmdl="nice -19 /usr/local/java.current/bin/java -mx256m -classpath /usr/staff/tapted/lib/grok-exe-0.6.0.jar:./nlpdemo.jar foldocpos.POSParse usability.dict.gz -lwc ucategories.txt -m 4 /dev/null"
echo "Running "$cmdl
echo "on cpu2 in /usr/staff/tapted/mecureo/dict"
nice -19 rsh cpu2 nice -19 /usr/staff/tapted/bin/runin /usr/staff/tapted/mecureo/dict/ nice -19 /usr/local/java.current/bin/java -mx256m -classpath /usr/staff/tapted/lib/grok-exe-0.6.0.jar:./nlpdemo.jar foldocpos.POSParse usability.dict.gz -lwc ucategories.txt -m 4 /dev/null
