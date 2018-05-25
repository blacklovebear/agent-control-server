#!/bin/bash

base=`dirname $0`
if [ -f $base/bin/tagent.pid ] ; then
  echo "found tagent.pid, please run stop.sh first, then start.sh" 2>&2
  exit 0
fi

nohup bin/flume-ng agent --conf conf --conf-file conf/canal-kafka.conf --name agent -Dflume.monitoring.type=http -Dflume.monitoring.port=34545 > logs/start.out 2>&1 &
echo $! > $base/bin/tagent.pid
