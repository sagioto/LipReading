/*
 * FeatureExtractor.h
 *
 *  Created on: 10 ???? 2013
 *      Author: Sagi
 */

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/objdetect/objdetect.hpp>
#include <opencv2/contrib/detection_based_tracker.hpp>
#include <android/log.h>

#include <string>
#include <iostream>
#include <vector>
#include <algorithm>



#ifndef FEATUREEXTRACTOR_H_
#define FEATUREEXTRACTOR_H_


#define LOG_TAG "LipReading/FeatureExtractor"
#define LOGE(...) ((void)__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__))
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))
#define SCALE 4
#define CHANNELS 4
#define RED 0
#define GREEN 1
#define BLUE 2
#define ROI_VERTICAL_JUMP 10
#define ROI_FRAME_THRESHOLD 10
#define SAMPLE_RATE 30


using namespace std;
using namespace cv;

class FeatureExtractor
{

	CascadeClassifier* classifier;
	Mat* scaled, * roi, * h, * L, * col, * R, * G, * B;
	vector<Rect>* mouths;
	vector<Mat>* channels;
	vector<int>* Lmini;
	Rect* mouth, * prev;
	Point* minLocation;
	int rectFrameCount, sampleRateCount;


public:
	FeatureExtractor(const string& haarFilePath, int w, int h);
	virtual ~FeatureExtractor();

	//put the points by the following order: R,L,U,D
	void detect(const Mat& grayImage, Mat& rgbaImage, int* points);
};

#endif /* FEATUREEXTRACTOR_H_ */
