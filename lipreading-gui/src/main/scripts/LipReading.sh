#!/bin/bash
export LD_LIBRARY_PATH=${LD_LIBRARY_PATH}:$PWD/bin
sudo ldconfig
javaw -jar bin/lipreading-gui-*.jar &
