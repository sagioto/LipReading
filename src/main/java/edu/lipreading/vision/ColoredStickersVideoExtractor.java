/**
 * 
 */
package edu.lipreading.vision;

import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSize;
import static com.googlecode.javacv.cpp.opencv_core.cvInRangeS;
import static com.googlecode.javacv.cpp.opencv_highgui.cvSaveImage;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_MEDIAN;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvGetCentralMoment;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvGetSpatialMoment;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvMoments;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvSmooth;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_imgproc.CvMoments;

/**
 * @author Dor
 *
 */
public class ColoredStickersVideoExtractor extends AbstractVideoExtractor{

	/**
	 * 
	 */
	public ColoredStickersVideoExtractor() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void savePoints(IplImage img) throws Exception {
		ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		List<Integer> frameCoordinates = new Vector<Integer>();
		List<Future<List<Integer>>> futuresList = new Vector<Future<List<Integer>>>();

		futuresList.add(threadPool.submit(new CoordinateGetter(img, VideoConfiguration.UPPER_STICKER_MIN, VideoConfiguration.UPPER_STICKER_MAX)));
		futuresList.add(threadPool.submit(new CoordinateGetter(img, VideoConfiguration.LOWER_STICKER_MIN, VideoConfiguration.LOWER_STICKER_MAX)));
		futuresList.add(threadPool.submit(new CoordinateGetter(img, VideoConfiguration.LEFT_STICKER_MIN, VideoConfiguration.LEFT_STICKER_MAX)));
		futuresList.add(threadPool.submit(new CoordinateGetter(img, VideoConfiguration.RIGHT_STICKER_MIN, VideoConfiguration.RIGHT_STICKER_MAX)));

		for (Future<List<Integer>> future : futuresList) {
			frameCoordinates.addAll(future.get());
		}

		sample.getMatrix().add(frameCoordinates);

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getPoints(IplImage img) throws Exception {
		ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		Vector <Integer> emptyList = new Vector<Integer>();
		emptyList.add(0);
		emptyList.add(0);
		
		List<Integer> frameCoordinates = new Vector<Integer>();
		List<Future<List<Integer>>> futuresList = new Vector<Future<List<Integer>>>();
		
		if (VideoConfiguration.UPPER_STICKER_MIN != null && VideoConfiguration.UPPER_STICKER_MAX != null)
			futuresList.add(threadPool.submit(new CoordinateGetter(img, VideoConfiguration.UPPER_STICKER_MIN, VideoConfiguration.UPPER_STICKER_MAX)));
		else
			futuresList.add(null);
		if (VideoConfiguration.LOWER_STICKER_MIN != null && VideoConfiguration.LOWER_STICKER_MAX != null)
			futuresList.add(threadPool.submit(new CoordinateGetter(img, VideoConfiguration.LOWER_STICKER_MIN, VideoConfiguration.LOWER_STICKER_MAX)));
		else
			futuresList.add(null);
		if (VideoConfiguration.LEFT_STICKER_MIN != null && VideoConfiguration.LEFT_STICKER_MAX != null)
			futuresList.add(threadPool.submit(new CoordinateGetter(img, VideoConfiguration.LEFT_STICKER_MIN, VideoConfiguration.LEFT_STICKER_MAX)));
		else
			futuresList.add(null);
		if (VideoConfiguration.RIGHT_STICKER_MIN != null && VideoConfiguration.RIGHT_STICKER_MAX != null)
			futuresList.add(threadPool.submit(new CoordinateGetter(img, VideoConfiguration.RIGHT_STICKER_MIN, VideoConfiguration.RIGHT_STICKER_MAX)));
		else
			futuresList.add(null);
		
		for (Future<List<Integer>> future : futuresList) {
			if (future == null)
				frameCoordinates.addAll((Vector<Integer>)emptyList.clone()); //Adds 0,0 if sticker color wasnt found
			else
				frameCoordinates.addAll(future.get());
		}

		return frameCoordinates;
	}
	
	class CoordinateGetter implements Callable<List<Integer>>{
		private IplImage grabbed;
		private CvScalar min; 
		private CvScalar max;

		public CoordinateGetter(IplImage grabbed, CvScalar min, CvScalar max) {
			super();
			this.grabbed = grabbed;
			this.min = min;
			this.max = max;
		}

		@Override
		public List<Integer> call() throws java.lang.Exception {
			return getCoordinatesOfObject(this.grabbed, this.min, this.max);
		}

	}

	private IplImage getThresholdImage(IplImage orgImg, CvScalar minScalar, CvScalar maxScalar) {
		IplImage imgThreshold = cvCreateImage(cvGetSize(orgImg), 8, 1);
		cvInRangeS(orgImg, minScalar, maxScalar, imgThreshold);
		//cvSmooth(imgThreshold, imgThreshold, CV_MEDIAN, 15);
		//cvSaveImage("dsmthreshold.jpg", imgThreshold);
		return imgThreshold;
	}

	private List<Integer> getCoordinatesOfObject(IplImage img, CvScalar scalarMin,
			CvScalar scalarMax) {
		IplImage detectThrs = getThresholdImage(img, scalarMin, scalarMax);
		CvMoments moments = new CvMoments();
		cvMoments(detectThrs, moments, 1);
		double mom10 = cvGetSpatialMoment(moments, 1, 0);
		double mom01 = cvGetSpatialMoment(moments, 0, 1);
		double area = cvGetCentralMoment(moments, 0, 0);
		int posX = (int) (mom10 / area);
		int posY = (int) (mom01 / area);
		List<Integer> ans = new Vector<Integer>();
		ans.add(posX);
		ans.add(posY);
		return ans;
	}
}
