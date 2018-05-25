#!/bin/bash
var=`ps -ef | grep flume.node.Application | grep -v grep | awk '{print $2}'`
if [ -z "$var" ]
then
  echo "tagent is not running"
else
  kill -15 $var
fi
rm -rf /tmp/tagent.lock
