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
wget ftp://ftp.videolan.org/pub/videolan/x264/snapshots/last_stable_x264.tar.bz2
tar xvf last_stable_x264.tar.bz2
cd x264*
./configure --enable-static
make
sudo make install

# 1.2) Download and install ffmpeg
echo "installing ffmepg"
cd ~/src
wget http://ffmpeg.org/releases/ffmpeg-1.0.tar.bz2
tar xvf ffmpeg-1.0.tar.bz2
cd ffmpeg-1.0

./configure --enable-gpl --enable-libfaac --enable-libmp3lame --enable-libopencore-amrnb --enable-libopencore-amrwb --enable-libtheora --enable-libvorbis --enable-libx264 --enable-libxvid --enable-nonfree --enable-postproc --enable-version3 --enable-x11grab --enable-shared
