// FeatureExtractor.cpp : Defines the entry point for the console application.
//

#include "FeatureExtractor.h"

using namespace std;
using namespace cv;


FeatureExtractor::FeatureExtractor(const string& haarFilePath, int w, int h)
{
	Size* s = new Size(w, h);
	this->classifier = new CascadeClassifier(haarFilePath);
	this->scaled = new Mat(*s, CV_8UC1),
			this->roi = new Mat(*s, CV_8UC4),
			this->h = new Mat(*s, CV_32FC1),
			this->L = new Mat(*s, CV_32FC1),
			this->Lmini = new vector<int>(w),
			this->R = new Mat(*s, CV_32FC1),
			this->G = new Mat(*s, CV_32FC1),
			this->B = new Mat(*s, CV_32FC1),
			this->col = new Mat(*s, CV_32FC1);
	this->mouths = new vector<Rect>();
	this->mouth = new Rect(), prev = new Rect();
	this->channels = new vector<Mat>();
	this->minLocation = new Point();
	this->rectFrameCount = 0, this->sampleRateCount = 0;
}

FeatureExtractor::~FeatureExtractor()
{
	delete this->classifier;
	delete this->scaled;
	delete this->roi;
	delete this->h;
	delete this->L;
	delete this->Lmini;
	delete this->R;
	delete this->G;
	delete this->B;
	delete this->col;
	delete this->mouths;
	delete this->mouth;
	delete this->prev;
	delete this->channels;
	delete this->minLocation;
}

void FeatureExtractor::detect(const Mat& grayImage, Mat& rgbaImage, int* points)
{
	LOGD("entered detect");
	int roiFix = grayImage.rows / -32;
	int	sideConfidence = grayImage.cols / 64;
	int	lowerConfidence = grayImage.rows / 86;
	int	upperConfidence = grayImage.cols / 8;
	bool found = false;

	if(this->sampleRateCount++ % SAMPLE_RATE == 0)
	{
		resize(grayImage, *scaled, Size(grayImage.cols / SCALE, grayImage.rows / SCALE));
		classifier->detectMultiScale(*scaled,
				*mouths,
				1.1,
				7,
				CASCADE_FIND_BIGGEST_OBJECT | CASCADE_DO_ROUGH_SEARCH | CASCADE_DO_CANNY_PRUNING);
		if(mouths->size() == 0){
			this->sampleRateCount = 0;
			LOGD("did'nt find mouth, leaving detect");
			return;
		}
		else if (prev && prev->y != 0 && rectFrameCount < ROI_FRAME_THRESHOLD)
		{
			if (abs(mouth->y - prev->y) > ROI_VERTICAL_JUMP){
				*mouth = *prev;
				rectFrameCount++;
			}
		}
		else if (rectFrameCount >= ROI_FRAME_THRESHOLD)
		{
			rectFrameCount = 0;
		}

		int i = 8;
		*mouth = (*mouths)[0];
		//stretch the roi again
		mouth->x *= SCALE;
		mouth->y *= SCALE;
		mouth->width *= SCALE;
		mouth->height *= SCALE;

		mouth->y += roiFix;

		//put in the roi rect for debugging
		points[i++] = mouth->tl().x;
		points[i++] = mouth->br().y;
		points[i++] = mouth->tl().x;
		points[i++] = mouth->br().y;
		*prev = *mouth;
	}
	else
	{
		*mouth = *prev;
	}

	*roi = rgbaImage(*mouth);
	split(*roi, *channels);
	((Mat)(*channels)[RED]).convertTo(*R, CV_32FC1);
	((Mat)(*channels)[GREEN]).convertTo(*G, CV_32FC1);
	((Mat)(*channels)[BLUE]).convertTo(*B, CV_32FC1);
	//get h
	//h = R / (G + R)
	*h = ((*R) / ((*G) + (*R)));
	*h /= *max_element(h->begin<float>(), h->end<float>());

	//get L
	//L = (R + R + B + G + G + G) / 6
	*L = (((*R) + (*R) + (*B) + (*G) + (*G) + (*G)) / 6);
	*L /= *max_element(L->begin<float>(), L->end<float>());

	double minVal = 0;
	double Lmean = 0;
	Lmini->reserve(L->cols);
	for(int i = 0; i < L->cols; i++)
	{
		minMaxLoc(L->col(i), &minVal, 0, minLocation);
		(*Lmini)[i] = minLocation->y;
		Lmean += minVal;
	}
	Lmean /= L->cols;


	//get right
	found = false;
	for (int i = L->cols - 1; i >= 0 && !found; i--)
	{
		if(L->at<float>((int)(*Lmini)[i], i) < Lmean)
		{
			found = true;
			for (int j = i; j > max(i - sideConfidence, 0) && found; j--)
			{
				found = L->at<float>((int)(*Lmini)[i], j) < Lmean;
			}
			if(found)
				points[0] = i + mouth->x, points[1] = ((int)(*Lmini)[i]) + mouth->y;
		}
	}

	//get left
	found = false;
	for (int i = 0; i < L->cols && !found; i++)
	{
		if(L->at<float>((int)(*Lmini)[i], i) < Lmean)
		{
			found = true;
			for (int j = i; j < min(i + sideConfidence, (int)Lmini->size()) && found; j++)
			{
				found = L->at<float>((int)(*Lmini)[i], j) < Lmean;
			}
			if(found)
				points[2] = i + mouth->x, points[3] = ((int)(*Lmini)[i]) + mouth->y;
		}
	}

	int centerLine = ((points[0] + points[2]) / 2) - mouth->x;

	*col = ((*h) - (*L)).col(centerLine);
	//get upper
	int y = 0;
	found = false;
	for(int i = 0; i < col->rows - 1 && !found; i++)
	{
		if(col->at<float>(i) <= col->at<float>(i + 1))
		{
			y = i;
			found = true;
			for (int j = i + 2; j < min(col->rows, i + upperConfidence) && found; j++)
			{
				found = col->at<float>(i) < col->at<float>(j);
			}
			if(found)
				points[4] = centerLine + mouth->x, points[5] =  i + mouth->y;
		}
	}

	//get down
	*col = L->col(centerLine);
	found = false;
	for (int i = col->rows - 1; i >= 0 && !found; i--)
	{
		found = true;//col->at<float>(i) < Lmean;
		for (int j = i; j > max(i - lowerConfidence, 0) && found; j--)
		{
			found = col->at<float>(j) < col->at<float>(j - 1);
			//found = col->at<float>(j) < Lmean;
		}
		if(found)
			points[6] = centerLine + mouth->x, points[7] =  i + mouth->y;
	}

	for(int i = 0; i < 8; i += 2)
		circle(rgbaImage, cvPoint(points[i], points[i + 1]), 2, Scalar(0, 255, 0), -1, CV_AA);

	LOGD("left detect");
	//rectangle(rgbaImage, *mouth, Scalar(0, 255, 0));
	//Lmini->clear();
}
