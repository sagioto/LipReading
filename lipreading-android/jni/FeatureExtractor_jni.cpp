#include <FeatureExtractor_jni.h>
#include <FeatureExtractor.h>
#include <string>


#include <android/log.h>

#define LOG_TAG "FaceDetection/DetectionBasedTracker"
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))

using namespace std;
using namespace cv;

JNIEXPORT jlong JNICALL Java_edu_lipreading_android_FeatureExtractor_nativeCreateObject
(JNIEnv * jenv, jclass, jstring jFileName, jint w, jint h)
{
    LOGD("Java_org_opencv_samples_facedetect_DetectionBasedTracker_nativeCreateObject enter");
    const char* jnamestr = jenv->GetStringUTFChars(jFileName, NULL);
    string stdFileName(jnamestr);
    jlong result = 0;

    try
    {
    	FeatureExtractor* fe = new FeatureExtractor(stdFileName, w, h);
    	result = (jlong)fe;
    }
    catch(cv::Exception& e)
    {
        LOGD("nativeCreateObject caught cv::Exception: %s", e.what());
        jclass je = jenv->FindClass("org/opencv/core/CvException");
        if(!je)
            je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, e.what());
    }
    catch (...)
    {
        LOGD("nativeCreateObject caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, "Unknown exception in JNI code {highgui::VideoCapture_n_1VideoCapture__()}");
        return 0;
    }

    LOGD("Java_org_opencv_samples_facedetect_DetectionBasedTracker_nativeCreateObject exit");
    return result;
}

JNIEXPORT void JNICALL Java_edu_lipreading_android_FeatureExtractor_nativeDestroyObject
(JNIEnv * jenv, jclass, jlong thiz)
{
    LOGD("Java_org_opencv_samples_facedetect_DetectionBasedTracker_nativeDestroyObject enter");
    try
    {
        if(thiz != 0)
        {
            delete (FeatureExtractor*)thiz;
        }
    }
    catch(cv::Exception& e)
    {
        LOGD("nativeestroyObject caught cv::Exception: %s", e.what());
        jclass je = jenv->FindClass("org/opencv/core/CvException");
        if(!je)
            je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, e.what());
    }
    catch (...)
    {
        LOGD("nativeDestroyObject caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, "Unknown exception in JNI code {highgui::VideoCapture_n_1VideoCapture__()}");
    }
    LOGD("Java_org_opencv_samples_facedetect_DetectionBasedTracker_nativeDestroyObject exit");
}

JNIEXPORT void JNICALL Java_edu_lipreading_android_FeatureExtractor_nativeDetect
(JNIEnv * jenv, jclass, jlong thiz, jlong grayImage, jlong rgbaImage, jintArray points)
{
    LOGD("Java_org_opencv_samples_facedetect_DetectionBasedTracker_nativeDetect enter");
    try
    {
    	jint* rpoints = jenv->GetIntArrayElements(points, NULL);
    	int * npoints = new int[12];
    	for(int i = 0; i < 12; i++)
    	        {
    	        	npoints[i] = 0;
    	        }

		cv::Mat* mat = (Mat*) (rgbaImage);
		cv::Mat* gmat = (Mat*) (grayImage);
		*mat = mat->t();
		*gmat = gmat->t();
		flip(*mat, *mat, 0);
		flip(*gmat, *gmat, 0);
		((FeatureExtractor*) thiz)->detect(*gmat, *mat, npoints);
		flip(*mat, *mat, 1);
        for(int i = 0; i < 12; i++)
        {
        	rpoints[i] = npoints[i];
        }
        jenv->ReleaseIntArrayElements(points, rpoints, 0);
        delete npoints;
    }
    catch(cv::Exception& e)
    {
        LOGD("nativeDetect caught cv::Exception: %s", e.what());
        jclass je = jenv->FindClass("org/opencv/core/CvException");
        if(!je)
            je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, e.what());
    }
    catch (...)
    {
        LOGD("nativeDetect caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, "Unknown exception in JNI code {highgui::VideoCapture_n_1VideoCapture__()}");
    }
    LOGD("Java_org_opencv_samples_facedetect_DetectionBasedTracker_nativeDetect exit");
}
