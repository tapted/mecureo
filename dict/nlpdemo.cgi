#!/bin/sh
echo Content-Type: text/plain
echo
nice -19 yes | nice -19 ./rundemo.sh 2>&1
