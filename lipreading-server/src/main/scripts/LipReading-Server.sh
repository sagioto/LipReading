#!/bin/bash
PATH=$PATH:$PWD/bin
java -jar -Xss2M bin/lipreading-server-*.jar &
