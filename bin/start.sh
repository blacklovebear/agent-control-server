#!/bin/bash

ps -ef | grep drc-streaming-control | grep -v grep | awk '{print $2}' | xargs kill -9
mkdir -p logs
nohup java -jar -Dserver.conf="./conf/application.properties" -Dtemplate.dir="./template" \
-Dlog4j.configuration="file:./conf/log4j.properties" drc-streaming-control*.jar > logs/start.out 2>&1 &
