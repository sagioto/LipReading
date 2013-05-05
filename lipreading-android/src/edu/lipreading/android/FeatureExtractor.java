package edu.lipreading.android;

import org.opencv.core.Mat;

public class FeatureExtractor {
	private static native long nativeCreateObject(String cascadeName, int w, int h);
    private static native void nativeDestroyObject(long thiz);
    private static native void nativeDetect(long thiz, long grayImage, long rgbaImage, int[] points);
    
    public FeatureExtractor(String cascadeName, int w, int h) {
        mNativeObj = nativeCreateObject(cascadeName, w, h);
    }

    
    public void detect(Mat grayImage, Mat rgbaImage, int[] points) {
        nativeDetect(mNativeObj, grayImage.getNativeObjAddr(), rgbaImage.getNativeObjAddr(), points);
    }

    public void release() {
        nativeDestroyObject(mNativeObj);
        mNativeObj = 0;
    }

    private long mNativeObj = 0;
}
