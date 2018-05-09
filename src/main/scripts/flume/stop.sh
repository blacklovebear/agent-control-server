#!/bin/bash
ps -ef | grep flume.node.Application | grep -v grep | awk '{print $2}' | xargs kill -15
