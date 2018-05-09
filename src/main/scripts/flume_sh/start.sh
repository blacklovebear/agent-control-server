#!/bin/bash

./stop.sh

nohup bin/flume-ng agent --conf conf --conf-file conf/canal-kafka.conf --name agent -Dflume.monitoring.type=http -Dflume.monitoring.port=34545 > logs/start.out 2>&1 &
