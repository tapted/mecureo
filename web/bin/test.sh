ulimit -t 1 -v 1
ulimit -a
nice -19 cat $@
if [ "$?" != "0" ] ; then
  echo "moo"
fi
