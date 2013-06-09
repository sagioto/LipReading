LipReading [![Build Status](https://secure.travis-ci.org/sagioto/LipReading.png)](http://travis-ci.org/sagioto/LipReading)
==========

## Goal

The goal of this project is to extract text words from a video input using visual lips tracking and machine learning techniques.

## Contributors

LipReading is the final project of Ben Gurion University Software Engineering students:
* [Sagi Bernstein](https://github.com/sagioto) [(LinkedIn)](http://www.linkedin.com/profile/view?id=103685568)
* [Dor Leitman](https://github.com/dorleitman)
* [Dagan Sandler](https://github.com/dagansandler) [(LinkedIn)](http://www.linkedin.com/profile/view?id=95457922)

This project is guided by [Dr. Kobi Gal](http://www.eecs.harvard.edu/~gal/) and Dr. Gavi Kohlberg

## Overview

We aspire to supply patients with larynx or vocal cord conditions an easy and intuitive way to communicate.
The system identifies the user’s lip movements by video, and transforms the video to audio output of the spoken word. The system works on any computer equipped with a basic camera.
The system is based on image-processing algorithms and machine learning.
Given a video segment, the image-processing algorithm identifies the lips and extracts the coordinates of a number of points on the lips from each frame.
This data goes through a series of normalization actions to improve the classification results.
The last stage of the classification is performed by Machine Learning algorithms implemented by third-party packages we use.
After classifying a given word, the audio output of the result is played.
