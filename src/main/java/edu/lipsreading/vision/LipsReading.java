package edu.lipsreading.vision;

import static com.googlecode.javacv.cpp.opencv_core.CV_AA;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvClearMemStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSeqElem;
import static com.googlecode.javacv.cpp.opencv_core.cvLoad;
import static com.googlecode.javacv.cpp.opencv_core.cvPoint;
import static com.googlecode.javacv.cpp.opencv_core.cvRectangle;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static com.googlecode.javacv.cpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;
import static com.googlecode.javacv.cpp.opencv_objdetect.cvHaarDetectObjects;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.FrameRecorder;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_objdetect;
import com.googlecode.javacv.cpp.opencv_objdetect.CvHaarClassifierCascade;


public class LipsReading {

	public static void main(String...args) throws Exception{
		 String classifierName = null;
	        if (args.length > 0) {
	            classifierName = args[0];
	        } else {
	            System.err.println("Please provide the path to \"haarcascade_frontalface_alt.xml\".");
	            System.exit(1);
	        }

	        // Preload the opencv_objdetect module to work around a known bug.
	        Loader.load(opencv_objdetect.class);

	        // We can "cast" Pointer objects by instantiating a new object of the desired class.
	        CvHaarClassifierCascade classifier = new CvHaarClassifierCascade(cvLoad(classifierName));
	        if (classifier.isNull()) {
	            System.err.println("Error loading classifier file \"" + classifierName + "\".");
	            System.exit(1);
	        }

	        // The available FrameGrabber classes include OpenCVFrameGrabber (opencv_highgui),
	        // DC1394FrameGrabber, FlyCaptureFrameGrabber, OpenKinectFrameGrabber,
	        // PS3EyeFrameGrabber, VideoInputFrameGrabber, and FFmpegFrameGrabber.
	        FrameGrabber grabber = FrameGrabber.createDefault(0);
	        grabber.start();

	        // FAQ about IplImage:
	        // - For custom raw processing of data, getByteBuffer() returns an NIO direct
	        //   buffer wrapped around the memory pointed by imageData, and under Android we can
	        //   also use that Buffer with Bitmap.copyPixelsFromBuffer() and copyPixelsToBuffer().
	        // - To get a BufferedImage from an IplImage, we may call getBufferedImage().
	        // - The createFrom() factory method can construct an IplImage from a BufferedImage.
	        // - There are also a few copy*() methods for BufferedImage<->IplImage data transfers.
	        IplImage grabbedImage = grabber.grab();
	        int width  = grabbedImage.width();
	        int height = grabbedImage.height();
	        IplImage grayImage    = IplImage.create(width, height, IPL_DEPTH_8U, 1);

	        // Objects allocated with a create*() or clone() factory method are automatically released
	        // by the garbage collector, but may still be explicitly released by calling release().
	        // You shall NOT call cvReleaseImage(), cvReleaseMemStorage(), etc. on objects allocated this way.
	        CvMemStorage storage = CvMemStorage.create();

	        // The OpenCVFrameRecorder class simply uses the CvVideoWriter of opencv_highgui,
	        // but FFmpegFrameRecorder also exists as a more versatile alternative.
	        FrameRecorder recorder = FrameRecorder.createDefault("output.avi", width, height);
	        recorder.start();

	        // CanvasFrame is a JFrame containing a Canvas component, which is hardware accelerated.
	        // It can also switch into full-screen mode when called with a screenNumber.
	        // We should also specify the relative monitor/camera response for proper gamma correction.
	        CanvasFrame frame = new CanvasFrame("Lips reading", CanvasFrame.getDefaultGamma()/grabber.getGamma());

	        // We can allocate native arrays using constructors taking an integer as argument.
	        while (frame.isVisible() && (grabbedImage = grabber.grab()) != null) {
	            cvClearMemStorage(storage);

	            // Let's try to detect some faces! but we need a grayscale image...
	            cvCvtColor(grabbedImage, grayImage, CV_BGR2GRAY);
	            CvSeq faces = cvHaarDetectObjects(grayImage, classifier, storage,
	                    2.8, 3, CV_HAAR_DO_CANNY_PRUNING);
	            int total = faces.total();
	            for (int i = 0; i < total; i++) {
	                CvRect r = new CvRect(cvGetSeqElem(faces, i));
	                int x = r.x(), y = r.y(), w = r.width(), h = r.height();
	                cvRectangle(grabbedImage, cvPoint(x, y), cvPoint(x+w, y+h), CvScalar.GREEN, 1, CV_AA, 0);

	            }

	            frame.showImage(grabbedImage);
	            recorder.record(grabbedImage);
	        }
	        frame.dispose();
	        recorder.stop();
	        grabber.stop();
	        
	        System.exit(0);
	}
}
