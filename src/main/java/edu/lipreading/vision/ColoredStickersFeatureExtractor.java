package edu.lipreading.vision;

import static com.googlecode.javacv.cpp.opencv_core.cvCircle;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSize;
import static com.googlecode.javacv.cpp.opencv_core.cvInRangeS;
import static com.googlecode.javacv.cpp.opencv_core.cvScalar;
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

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.FFmpegFrameRecorder;
import com.googlecode.javacv.FrameRecorder;
import com.googlecode.javacv.cpp.opencv_core.CvArr;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_imgproc.CvMoments;

import edu.lipreading.Sample;
import edu.lipreading.Utils;

public class ColoredStickersFeatureExtractor extends AbstractFeatureExtractor{
	private final static short NUM_OF_STICKERS = 4;

	private final static short RED_VECTOR_INDEX = 0;
	private final static short GREEN_VECTOR_INDEX = 1;
	private final static short BLUE_VECTOR_INDEX = 2;
	private final static short YELLOW_VECTOR_INDEX = 3;


	private final static CvScalar RED_MIN = cvScalar(20, 40, 200, 0); 
	private final static CvScalar RED_MAX = cvScalar(40, 80, 255, 0);

	private final static CvScalar GREEN_MIN = cvScalar(0, 30, 0, 0);
	private final static CvScalar GREEN_MAX = cvScalar(70, 255, 70, 0);

	private final static CvScalar BLUE_MIN = cvScalar(70, 0, 10, 0);
	private final static CvScalar BLUE_MAX = cvScalar(170, 60, 70, 0);

	private final static CvScalar YELLOW_MIN = cvScalar(20, 100, 100, 0);
	private final static CvScalar YELLOW_MAX = cvScalar(50, 255, 255, 0);


	@Override
	protected Sample getPoints() throws Exception {
		ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		Sample sample = new Sample(sampleName);
		IplImage grabbed;
		CanvasFrame frame = null;
		FrameRecorder recorder = null;

		if(!Utils.isCI()){
			frame = new CanvasFrame(sampleName, CanvasFrame.getDefaultGamma()/grabber.getGamma());
			frame.setDefaultCloseOperation(CanvasFrame.EXIT_ON_CLOSE);
			if(isOutput()){
				recorder = FFmpegFrameRecorder.createDefault(sampleName.split("\\.")[0] + "-output.MOV",grabber.getImageWidth(), grabber.getImageHeight());
				recorder.start();
			}
		}

		while((grabbed = grabber.grab()) != null){
			List<Integer> frameCoordinates = new Vector<Integer>();
			List<Future<List<Integer>>> futuresList = new Vector<Future<List<Integer>>>();

			futuresList.add(threadPool.submit(new CoordinateGetter(grabbed, RED_MIN, RED_MAX)));
			futuresList.add(threadPool.submit(new CoordinateGetter(grabbed, GREEN_MIN, GREEN_MAX)));
			futuresList.add(threadPool.submit(new CoordinateGetter(grabbed, BLUE_MIN, BLUE_MAX)));
			futuresList.add(threadPool.submit(new CoordinateGetter(grabbed, YELLOW_MIN, YELLOW_MAX)));

			for (Future<List<Integer>> future : futuresList) {
				frameCoordinates.addAll(future.get());
			}

			sample.getMatrix().add(frameCoordinates);
			if(!Utils.isCI()){
				for(int i = 0; i < NUM_OF_STICKERS; i++){
					CvScalar color = null;
					switch (i){
					case RED_VECTOR_INDEX:
						color = cvScalar(0, 0, 255, 0);
						break;
					case GREEN_VECTOR_INDEX:
						color = cvScalar(0, 255, 0, 0);
						break;
					case BLUE_VECTOR_INDEX:
						color = cvScalar(255, 0, 0, 0);
						break;
					case YELLOW_VECTOR_INDEX:
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
		if(!Utils.isCI()){
			frame.dispose();
			if(isOutput()){
				recorder.stop();
			}
		}
		return sample;
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
		cvSmooth(imgThreshold, imgThreshold, CV_MEDIAN, 15);
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
