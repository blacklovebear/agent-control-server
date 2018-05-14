@echo off
@if not "%ECHO%" == ""  echo %ECHO%
@if "%OS%" == "Windows_NT"  setlocal

java -jar -Dserver.conf="./conf/application.properties" -Dtemplate.dir="./template" -Dlog4j.configuration="file:./conf/log4j.properties" drc-streaming-control*.jar
