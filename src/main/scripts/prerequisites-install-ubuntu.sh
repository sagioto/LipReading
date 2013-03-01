#!/bin/bash

# http://www.ozbotz.org/opencv-installation/
# http://shambool.com/2012/05/05/install-opencv-24-on-ubuntu-1204/

sudo apt-get remove ffmpeg x264 libx264-dev

sudo apt-get update
sudo apt-get install build-essential checkinstall git cmake libfaac-dev libjack-jackd2-dev libmp3lame-dev libopencore-amrnb-dev libopencore-amrwb-dev libsdl1.2-dev libtheora-dev libva-dev libvdpau-dev libvorbis-dev libx11-dev libxfixes-dev libxvidcore-dev texi2html yasm zlib1g-dev

mkdir ~/src
cd ~/src

echo "installing x264 ffmpeg gstreamr gtk v4l and libjpeg go for coffee..."

# 1.1) Download and install x264
echo "installing x264"
wget ftp://ftp.videolan.org/pub/videolan/x264/snapshots/x264-snapshot-20121231-2245-stable.tar.bz2
tar xvf x264-snapshot-20121231-2245-stable.tar.bz2
cd x264*
./configure --enable-shared
make
sudo make install

# 1.2) Download and install ffmpeg
echo "installing ffmepg"
cd ~/src
wget http://ffmpeg.org/releases/ffmpeg-1.0.1.tar.bz2
tar xvf ffmpeg-1.0.1.tar.bz2
cd ffmpeg-1.0.1
./configure --enable-gpl --enable-libfaac --enable-libmp3lame --enable-libopencore-amrnb --enable-libopencore-amrwb --enable-libtheora --enable-libvorbis --enable-libx264 --enable-libxvid --enable-nonfree --enable-postproc --enable-version3 --enable-x11grab --enable-shared
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
