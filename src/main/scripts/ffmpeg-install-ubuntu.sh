#!/bin/bash

# http://www.ozbotz.org/opencv-installation/
# http://shambool.com/2012/05/05/install-opencv-24-on-ubuntu-1204/

# 1.2)install ffmpeg
echo "installing ffmepg"
cd ~/src/ffmpeg-1.0
make
sudo make install

if [ "$USER" != "travis" ]; then
	# 2) Download and install gstreamer.

	sudo apt-get install -y libgstreamer0.10-0 libgstreamer0.10-dev gstreamer0.10-tools gstreamer0.10-plugins-base libgstreamer-plugins-base0.10-dev gstreamer0.10-plugins-good gstreamer0.10-plugins-ugly gstreamer0.10-plugins-bad gstreamer0.10-ffmpeg

	# 3) Download and install gtk.

	sudo apt-get install -y libgtk2.0-0 libgtk2.0-dev

	# 4) Download and install libjpeg.

	sudo apt-get install -y libjpeg8 libjpeg8-dev

	# 5) Download and install install a recent version of v4l (video for linux) from http://www.linuxtv.org/downloads/v4l-utils/. For this guide I used version 0.8.8.
	wget http://www.linuxtv.org/downloads/v4l-utils/v4l-utils-0.8.8.tar.bz2
	tar xvf v4l-utils-0.8.8.tar.bz2
	cd v4l-utils-0.8.8
	make
	sudo make install
else
	echo "travis!! no need to install gstreamer, gtk, jpeg, v4l"
fi
