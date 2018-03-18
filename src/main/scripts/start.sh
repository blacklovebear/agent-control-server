#!/usr/bin/bash

java -jar -Dserver.conf="./conf/application.properties" -Dtemplate.dir="./template" -Dlog4j.configuration="file:./conf/log4j.properties" agent-control-server-1.0.jar
