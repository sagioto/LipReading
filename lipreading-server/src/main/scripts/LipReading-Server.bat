@echo off
set PATH=%PATH%;%CD%\bin
start java -Xss2M -jar bin/lipreading-server-3.0.0.jar
