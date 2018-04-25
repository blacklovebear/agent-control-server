#!/usr/bin/bash

ps -ef | grep agent-control-server | grep -v grep | awk '{print $2}' | xargs kill -9

nohup java -jar -Dserver.conf="./conf/application.properties" -Dtemplate.dir="./template" \
-Dlog4j.configuration="file:./conf/log4j.properties" agent-control-server-1.0.jar > logs/start.out 2>&1 &
