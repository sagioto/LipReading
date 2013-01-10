package edu.lipreading.vision;

import static com.googlecode.javacv.cpp.opencv_core.cvCircle;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSize;
import static com.googlecode.javacv.cpp.opencv_core.cvInRangeS;
import static com.googlecode.javacv.cpp.opencv_core.cvScalar;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvGetCentralMoment;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvGetSpatialMoment;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvMoments;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.FFmpegFrameRecorder;
import com.googlecode.javacv.FrameRecorder;
import com.googlecode.javacv.cpp.opencv_core.CvArr;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_imgproc.CvMoments;

import edu.lipreading.Constants;
import edu.lipreading.Sample;

public class ColoredStickersFeatureExtractor extends AbstractFeatureExtractor{
	private final static ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	private final static short NUM_OF_STICKERS = (short) Constants.POINT_COUNT;

	@Override
	protected Sample getPoints() throws Exception {
		IplImage grabbed;
		CanvasFrame frame = null;
		FrameRecorder recorder = null;

		if(isGui()){
			frame = new CanvasFrame(getSample().getId(), CanvasFrame.getDefaultGamma()/grabber.getGamma());
			frame.setDefaultCloseOperation(CanvasFrame.EXIT_ON_CLOSE);
			if(isOutput()){
				recorder = FFmpegFrameRecorder.createDefault(getSample().getId().split("\\.")[0] + "-output.MOV",grabber.getImageWidth(), grabber.getImageHeight());
				recorder.start();
			}
		}

		while((grabbed = grabber.grab()) != null){
			List<Integer> frameCoordinates = getPoints(grabbed);

			getSample().getMatrix().add(frameCoordinates);
			if(isGui()){
				for(int i = 0; i < NUM_OF_STICKERS; i++){
					CvScalar color = null;
					switch (i){
					case Constants.UPPER_VECTOR_INDEX:
						color = cvScalar(0, 0, 255, 0);
						break;
					case Constants.LOWER_VECTOR_INDEX:
						color = cvScalar(0, 255, 0, 0);
						break;
					case Constants.LEFT_VECTOR_INDEX:
						color = cvScalar(255, 0, 0, 0);
						break;
					case Constants.RIGHT_VECTOR_INDEX:
						color = cvScalar(0, 242, 255, 0);
						break;
					}
					cvCircle((CvArr)grabbed, new CvPoint(frameCoordinates.get(i * 2), frameCoordinates.get((i * 2) + 1)), 25, color, 3, 0, 0);
					frame.showImage(grabbed);
					if(isOutput()){
						recorder.record(grabbed);
					}
				}
			}
		}
		if(isGui()){
			frame.dispose();
			if(isOutput()){
				recorder.stop();
			}
		}
		return getSample();
	}
	
	public List<Integer> getPoints(IplImage img)
			throws InterruptedException, ExecutionException {
		List<Integer> frameCoordinates = new Vector<Integer>();
		List<Future<List<Integer>>> futuresList = new Vector<Future<List<Integer>>>();

		futuresList.add(threadPool.submit(new CoordinateGetter(img, StickerColorConfiguration.UPPER_STICKER_MIN, StickerColorConfiguration.UPPER_STICKER_MAX)));
		futuresList.add(threadPool.submit(new CoordinateGetter(img, StickerColorConfiguration.LOWER_STICKER_MIN, StickerColorConfiguration.LOWER_STICKER_MAX)));
		futuresList.add(threadPool.submit(new CoordinateGetter(img, StickerColorConfiguration.LEFT_STICKER_MIN, StickerColorConfiguration.LEFT_STICKER_MAX)));
		futuresList.add(threadPool.submit(new CoordinateGetter(img, StickerColorConfiguration.RIGHT_STICKER_MIN, StickerColorConfiguration.RIGHT_STICKER_MAX)));

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
