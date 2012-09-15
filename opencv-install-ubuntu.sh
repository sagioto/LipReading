#!/bin/bash

# Also check there excellent guides: 
# http://www.ozbotz.org/opencv-installation/
# http://shambool.com/2012/05/05/install-opencv-24-on-ubuntu-1204/

# 1) Must run ffmpeg-install-ubuntu-12.04.sh before running this script.

# 2) Download and install gstreamer.

sudo apt-get install -y libgstreamer0.10-0 libgstreamer0.10-dev gstreamer0.10-tools gstreamer0.10-plugins-base libgstreamer-plugins-base0.10-dev gstreamer0.10-plugins-good gstreamer0.10-plugins-ugly gstreamer0.10-plugins-bad gstreamer0.10-ffmpeg

# 3) Download and install gtk.

sudo apt-get install -y libgtk2.0-0 libgtk2.0-dev

# 4) Download and install libjpeg.

sudo apt-get install -y libjpeg8 libjpeg8-dev

# 5) Download and install install a recent version of v4l (video for linux) from http://www.linuxtv.org/downloads/v4l-utils/. For this guide I used version 0.8.8.

cd ~/src
wget http://www.linuxtv.org/downloads/v4l-utils/v4l-utils-0.8.8.tar.bz2
tar xvf v4l-utils-0.8.8.tar.bz2
cd v4l-utils-0.8.8
make
sudo make install

# 5) Download and install install OpenCV 2.4.2.
# a) Download OpenCV version 2.4.2 from http://sourceforge.net/projects/opencvlibrary/files/

cd ~/src
wget http://downloads.sourceforge.net/project/opencvlibrary/opencv-unix/2.4.2/OpenCV-2.4.2.tar.bz2
tar xvf OpenCV-2.4.2.tar.bz2

# b) Create a new build directory and run cmake:
# NOTE: Adding BUILD_PERF_TESTS=0 to disable performance tests because of ffmpeg linking errors.

cd OpenCV-2.4.2/
mkdir build
cd build
cmake -D BUILD_PERF_TESTS=0 -D BUILD_EXAMPLES=0 -D BUILD_TESTS=0 -D CMAKE_BUILD_TYPE=RELEASE ..
#cmake -D CMAKE_BUILD_TYPE=RELEASE ..

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

# Add the command to your .bashrc file so that you don’t have to enter every time your start a new terminal.
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
