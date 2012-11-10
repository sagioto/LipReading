#!/bin/bash

# http://www.ozbotz.org/opencv-installation/
# http://shambool.com/2012/05/05/install-opencv-24-on-ubuntu-1204/

# 5) Download and install install OpenCV 2.4.3.
# a) Download OpenCV version 2.4.3 from http://sourceforge.net/projects/opencvlibrary/files/

cd ~/src
wget http://downloads.sourceforge.net/project/opencvlibrary/opencv-unix/2.4.3/OpenCV-2.4.3.tar.bz2
tar xvf OpenCV-2.4.3.tar.bz2

# b) Create a new build directory and run cmake:
# NOTE: Adding BUILD_PERF_TESTS=0 to disable performance tests because of ffmpeg linking errors.

cd OpenCV-2.4.3/
mkdir build
cd build
cmake -D BUILD_PERF_TESTS=0 -D BUILD_EXAMPLES=0 -D BUILD_TESTS=0 -D CMAKE_BUILD_TYPE=RELEASE ..

# c) Verify that the output of cmake includes the following text:
#    found gstreamer-base-0.10
#    GTK+ 2.x: YES
#    FFMPEG: YES
#    GStreamer: YES
#    V4L/V4L2: Using libv4l
    
# d) Build and install OpenCV.

make
sudo make install

# 6) Configure Linux.
# a) Tell linux where the shared libraries for OpenCV are located by entering the following shell command:

export LD_LIBRARY_PATH=/usr/local/lib

# Add the command to your .bashrc file so that you don�t have to enter every time your start a new terminal.
#
# Alternatively, you can configure the system wide library search path. 
# Using your favorite editor, add a single line containing the text /usr/local/lib to the end of a file named /etc/ld.so.conf.d/opencv.conf. 
# Using vi, for example, enter the following commands:
#
#    sudo vi /etc/ld.so.conf.d/opencv.conf
#    G
#    o
#    /usr/local/lib
#    <Esc>
#    :wq!
#
# After editing the opencv.conf file, enter the following command:
#
#    sudo ldconfig /etc/ld.so.conf

sudo cat <<'EOF' > /etc/ld.so.conf.d/opencv.conf
  /usr/local/lib
EOF
sudo ldconfig /etc/ld.so.conf


# b) Using your favorite editor, add the following two lines to the end of /etc/bash.bashrc:
#
#    PKG_CONFIG_PATH=$PKG_CONFIG_PATH:/usr/local/lib/pkgconfig
#    export PKG_CONFIG_PATH

# After completing the previous steps, your system should be ready to compile code that uses the OpenCV libraries. 
# The following example shows one way to compile code for OpenCV:
#
# g++ `pkg-config opencv --cflags` my_code.cpp  -o my_code `pkg-config opencv --libs` 
