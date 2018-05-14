#!/bin/bash

ps -ef | grep drc-streaming-control | grep -v grep | awk '{print $2}' | xargs kill -9