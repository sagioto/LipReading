package edu.lipreading.vision;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_objdetect;
import edu.lipreading.Constants;
import edu.lipreading.Utils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Vector;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static com.googlecode.javacv.cpp.opencv_highgui.cvSaveImage;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_objdetect.cvHaarDetectObjects;

/**
 * Created with IntelliJ IDEA.
 * User: Dor
 * Date: 05/05/13
 * Time: 11:24
 */
public class EyesFeatureExtractor extends AbstractFeatureExtractor{

    // The cascade definition to be used for detection.
    private static String CASCADE_FILE = null;

    public EyesFeatureExtractor(){
        try {
            CASCADE_FILE = Utils.getFileNameFromUrl(Constants.HAAR_CASCADE_EYES_FILE);
            if(!new File(CASCADE_FILE).exists()){
                Utils.get(Constants.HAAR_CASCADE_EYES_FILE);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void paintCoordinates(IplImage grabbed, List<Integer> frameCoordinates) {
        CvPoint leftEye = new CvPoint(frameCoordinates.get(0), frameCoordinates.get(1));
        CvPoint rightEye = new CvPoint(frameCoordinates.get(2), frameCoordinates.get(3));

        cvCircle(grabbed, leftEye, 2, CvScalar.RED, 2, 0, 0);
        cvCircle(grabbed, rightEye, 2, CvScalar.RED, 2, 0, 0);

    }

    @Override
    public List<Integer> getPoints(IplImage grabbed) throws Exception {
        List<Integer> eyesCoordinates = new Vector<Integer>();

        // We need a grayscale image in order to do the recognition, so we
        // create a new image of the same size as the original one.
        IplImage grayImage = IplImage.create(grabbed.width(),grabbed.height(), IPL_DEPTH_8U, 1);

        // We convert the original image to grayscale.
        cvCvtColor(grabbed, grayImage, CV_BGR2GRAY);

        CvMemStorage storage = CvMemStorage.create();

        // We instantiate a classifier cascade to be used for detection, using the cascade definition.
        opencv_objdetect.CvHaarClassifierCascade cascade = new opencv_objdetect.CvHaarClassifierCascade(
                cvLoad(CASCADE_FILE));

        // We detect the eyes.
        CvSeq eyes = cvHaarDetectObjects(grayImage, cascade, storage, 1.1, 1, 0);

        // If only one eyes area was detected then return eyes coordinates
        if (eyes.total() == 1){
            CvRect r = new CvRect(cvGetSeqElem(eyes, 0));

            int upperY = (int)(r.y() + (r.height()/4.5));
            int leftX = r.x() +r.width()/16;
            int lowerY =  r.y() + r.height();
            int rightX = r.x() + r.width();

            int centerX = (int)(leftX + rightX)/2;
            int centerY = (int)(upperY + lowerY)/2;

            int leftEyeX = (int)(centerX + leftX)/2;
            int rightEyeX = (int)(centerX + rightX)/2;

            eyesCoordinates.add(leftEyeX);
            eyesCoordinates.add(centerY);
            eyesCoordinates.add(rightEyeX);
            eyesCoordinates.add(centerY);

            return eyesCoordinates;
        }
        else{ // More than 2 eyes detected - not good... return null and try again in next frame
            return null;
        }
    }
}
