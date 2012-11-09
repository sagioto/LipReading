package edu.lipseading.vision;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvFlip;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSize;
import static com.googlecode.javacv.cpp.opencv_core.cvInRangeS;
import static com.googlecode.javacv.cpp.opencv_core.cvScalar;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_MEDIAN;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvEqualizeHist;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvGetCentralMoment;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvGetSpatialMoment;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvMoments;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvSmooth;
import static com.googlecode.javacv.cpp.opencv_highgui.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.VideoInputFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_imgproc.CvMoments;

public class ColoredObjectTrack implements Runnable {
    final int INTERVAL = 1000;// 1sec
    final int CAMERA_NUM = 0; // Default camera for this time

    /**
     * Correct the color range- it depends upon the object, camera quality,
     * environment.
     */
    private final static CvScalar red_min = cvScalar(0, 0, 130, 0);// RED 
    private final static CvScalar red_max = cvScalar(80, 80, 255, 0);
    
    private final static CvScalar green_min = cvScalar(0, 40, 0, 0);
    private final static CvScalar green_max = cvScalar(50, 255, 50, 0);

    private final static CvScalar blue_min = cvScalar(150, 0, 0, 0);
    private final static CvScalar blue_max = cvScalar(255, 150, 150, 0);
    
    private final static CvScalar yellow_min = cvScalar(0, 120, 120, 0);
    private final static CvScalar yellow_max = cvScalar(90, 255, 255, 0);
    
    
    static Color red = Color.RED;
    static Color green = Color.GREEN;
    static Color blue = Color.BLUE;
    static Color yellow = Color.YELLOW;
    
    
    IplImage image;
    CanvasFrame canvas = new CanvasFrame("Web Cam Live");
    CanvasFrame path = new CanvasFrame("Detection");
    int ii = 0;
    JPanel jp = new JPanel();

    public ColoredObjectTrack() {
        canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        path.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        path.setContentPane(jp);
        
    }

    @Override
    public void run() {
        FrameGrabber grabber = new VideoInputFrameGrabber(CAMERA_NUM);
        try {
            grabber.start();
            IplImage img;
            int posX = 0;
            int posY = 0;
            while (true) {
                img = grabber.grab();
                path.setSize(img.width(), img.height());
                if (img != null) {
                    // show image on window
                    cvFlip(img, img, 1);// l-r = 90_degrees_steps_anti_clockwise
                    canvas.showImage(img);
                    //IplImage detectThrs = getThresholdImage(img);
                    /*
                    // Option 1 - show first red, then blue, then green & yellow
                    showObject(img, red_min, red_max, red);
                    showObject(img, blue_min, blue_max, blue);
                    showObject(img, green_min, green_max, green);
                    showObject(img, yellow_min, yellow_max, yellow);
                    */
                    
                    // Option 2 - get threshold img of all colors & paint them together:
                    int[] redCoordinates = getCoordinatesOfObject(img, red_min, red_max);
                    int[] blueCoordinates = getCoordinatesOfObject(img, blue_min, blue_max);
                    int[] greenCoordinates = getCoordinatesOfObject(img, green_min, green_max);
                    int[] yellowCoordinates = getCoordinatesOfObject(img, yellow_min, yellow_max);
                    
                    Graphics redDot = jp.getGraphics();
                    redDot.clearRect(0, 0, img.width(), img.height());
                    redDot.setColor(red);
                    redDot.fillOval(redCoordinates[0],redCoordinates[1], 10, 10);
                    
                    Graphics blueDot = jp.getGraphics();
                    blueDot.clearRect(0, 0, img.width(), img.height());
                    blueDot.setColor(blue);
                    blueDot.fillOval(blueCoordinates[0], blueCoordinates[1], 10, 10);
                    
                    Graphics greenDot = jp.getGraphics();
                    greenDot.clearRect(0, 0, img.width(), img.height());
                    greenDot.setColor(green);
                    greenDot.fillOval(greenCoordinates[0], greenCoordinates[1], 10, 10);
                    
                    Graphics yellowDot = jp.getGraphics();
                    yellowDot.clearRect(0, 0, img.width(), img.height());
                    yellowDot.setColor(yellow);
                    //yellowDot.fillOval(yellowCoordinates[0], yellowCoordinates[1], 10, 10);
                    
                    redDot.drawOval(redCoordinates[0],redCoordinates[1], 10, 10);
                    blueDot.drawOval(blueCoordinates[0],blueCoordinates[1], 10, 10);
                    yellowDot.drawOval(yellowCoordinates[0],yellowCoordinates[1], 10, 10);
                    greenDot.drawOval(greenCoordinates[0],greenCoordinates[1], 10, 10);
                    
                }
                // Thread.sleep(INTERVAL);
            }
        } catch (Exception e) {
        }
    }

	private int[] getCoordinatesOfObject(IplImage img, CvScalar scalarMin,
			CvScalar scalarMax) {
		int posX;
		int posY;
		IplImage detectThrs = getThresholdImage(img, scalarMin, scalarMax);
		CvMoments moments = new CvMoments();
		cvMoments(detectThrs, moments, 1);
		double mom10 = cvGetSpatialMoment(moments, 1, 0);
		double mom01 = cvGetSpatialMoment(moments, 0, 1);
		double area = cvGetCentralMoment(moments, 0, 0);
		posX = (int) (mom10 / area);
		posY = (int) (mom01 / area);
		int[] res = {posX, posY}; 
		return res;
	}

	private void showObject(IplImage img, CvScalar scalarMin, CvScalar scalarMax, Color color) {
		int posX;
		int posY;
		IplImage detectThrs = getThresholdImage(img, scalarMin, scalarMax);
		CvMoments moments = new CvMoments();
		cvMoments(detectThrs, moments, 1);
		double mom10 = cvGetSpatialMoment(moments, 1, 0);
		double mom01 = cvGetSpatialMoment(moments, 0, 1);
		double area = cvGetCentralMoment(moments, 0, 0);
		posX = (int) (mom10 / area);
		posY = (int) (mom01 / area);
		// only if its a valid position
		if (posX > 0 && posY > 0) {
		    paint(img, posX, posY, color);
		}
	}

    private void paint(IplImage img, int posX, int posY, Color color) {
        Graphics g = jp.getGraphics();
        path.setSize(img.width(), img.height());

        g.clearRect(0, 0, img.width(), img.height());
        g.setColor(color);
        g.fillOval(posX, posY, 10, 10);
        g.drawOval(posX, posY, 10, 10);
       // System.out.println(posX + " , " + posY);
        

    }

    private IplImage getThresholdImage(IplImage orgImg) {

    	
    	IplImage imgThreshold = cvCreateImage(cvGetSize(orgImg), 8, 1);
        //
        cvInRangeS(orgImg, red_min, red_max, imgThreshold);// red

        cvSmooth(imgThreshold, imgThreshold, CV_MEDIAN, 15);
        cvSaveImage(++ii + "dsmthreshold.jpg", imgThreshold);
        return imgThreshold;
    }
    
    private IplImage getThresholdImage(IplImage orgImg, CvScalar minScalar, CvScalar maxScalar) {
        IplImage imgThreshold = cvCreateImage(cvGetSize(orgImg), 8, 1);
        //
        cvInRangeS(orgImg, minScalar, maxScalar, imgThreshold);// Color chosen

        cvSmooth(imgThreshold, imgThreshold, CV_MEDIAN, 15);
        //cvSaveImage(++ii + "dsmthreshold.jpg", imgThreshold);
    	//cvSaveImage((++ii)+"-aa.jpg", orgImg);
        return imgThreshold;
    }

    public static void main(String[] args) {
        ColoredObjectTrack cot = new ColoredObjectTrack();
        Thread th = new Thread(cot);
        th.start();
    }

    public IplImage Equalize(BufferedImage bufferedimg) {
        IplImage iploriginal = IplImage.createFrom(bufferedimg);
        IplImage srcimg = IplImage.create(iploriginal.width(), iploriginal.height(), IPL_DEPTH_8U, 1);
        IplImage destimg = IplImage.create(iploriginal.width(), iploriginal.height(), IPL_DEPTH_8U, 1);
        cvCvtColor(iploriginal, srcimg, CV_BGR2GRAY);
        cvEqualizeHist(srcimg, destimg);
        return destimg;
    }
}
