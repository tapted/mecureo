#!/bin/sh
today=`date +%Y%m%d`
last=""

for i in `find -type d | sort | tail ` ; do
  if [ "$i" -ge "$today" ] ; then
    break
  elif [ ! -z "$last" ] ; then
    rm -r "$last"
  fi
  last=$i
done


