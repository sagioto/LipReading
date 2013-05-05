package edu.lipreading.vision;

import edu.lipreading.Constants;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;

/**
 * This FeatureExtractor extracts lip coordinates according to colored stickers on user lips.
 * Stickers colors can be configured in Video Configuration tab in UI
 */
public class ColoredStickersFeatureExtractor extends AbstractFeatureExtractor{
	private final ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	public List<Integer> getPoints(IplImage img) throws Exception {
		List<Integer> frameCoordinates = new Vector<Integer>();
		List<Future<List<Integer>>> futuresList = new Vector<Future<List<Integer>>>();

        futuresList.add(threadPool.submit(new CoordinateGetter(img, StickerColorConfiguration.RIGHT_STICKER_MIN, StickerColorConfiguration.RIGHT_STICKER_MAX)));
        futuresList.add(threadPool.submit(new CoordinateGetter(img, StickerColorConfiguration.LEFT_STICKER_MIN, StickerColorConfiguration.LEFT_STICKER_MAX)));
        futuresList.add(threadPool.submit(new CoordinateGetter(img, StickerColorConfiguration.UPPER_STICKER_MIN, StickerColorConfiguration.UPPER_STICKER_MAX)));
        futuresList.add(threadPool.submit(new CoordinateGetter(img, StickerColorConfiguration.LOWER_STICKER_MIN, StickerColorConfiguration.LOWER_STICKER_MAX)));

		for (Future<List<Integer>> future : futuresList) {
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
		return imgThreshold;
	}

	private List<Integer> getCoordinatesOfObject(IplImage img, CvScalar scalarMin,
			CvScalar scalarMax) {
		IplImage thresholdImage = getThresholdImage(img, scalarMin, scalarMax);
		CvMoments moments = new CvMoments();
		cvMoments(thresholdImage, moments, 1);
		double mom10 = cvGetSpatialMoment(moments, 1, 0);
		double mom01 = cvGetSpatialMoment(moments, 0, 1);
		double area = cvGetCentralMoment(moments, 0, 0);
		int posX = (int) (mom10 / area);
		int posY = (int) (mom01 / area);
		List<Integer> ans = new Vector<Integer>();
		ans.add(posX);
		ans.add(posY);
		cvReleaseImage(thresholdImage);
		return ans;
	}

	@Override
	public void paintCoordinates(IplImage grabbed,
			List<Integer> frameCoordinates) {
		for (int i=0; i< Constants.POINT_COUNT; i++){
			CvScalar color = null;
			switch (i){
			case Constants.UPPER_VECTOR_INDEX:
				color = StickerColorConfiguration.UPPER_STICKER_MAX;
				break;
			case Constants.LOWER_VECTOR_INDEX:
				color = StickerColorConfiguration.LOWER_STICKER_MAX;
				break;
			case Constants.LEFT_VECTOR_INDEX:
				color = StickerColorConfiguration.LEFT_STICKER_MAX;
				break;
			case Constants.RIGHT_VECTOR_INDEX:
				color = StickerColorConfiguration.RIGHT_STICKER_MAX;
				break;
			}
			int x = frameCoordinates.get(i * 2);
			int y = frameCoordinates.get((i * 2) + 1);
			if (x != 0 && y!=0)
				cvCircle(grabbed, new CvPoint(x, y), 20, color, 3, 0, 0);
		}
	}

}
