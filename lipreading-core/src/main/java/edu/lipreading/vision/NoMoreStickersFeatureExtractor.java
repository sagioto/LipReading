package edu.lipreading.vision;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvCircle;
import static com.googlecode.javacv.cpp.opencv_core.cvClearMemStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSeqElem;
import static com.googlecode.javacv.cpp.opencv_core.cvLoad;
import static com.googlecode.javacv.cpp.opencv_core.cvResetImageROI;
import static com.googlecode.javacv.cpp.opencv_core.cvSetImageROI;
import static com.googlecode.javacv.cpp.opencv_core.cvSize;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_COUNTER_CLOCKWISE;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_RGB2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static com.googlecode.javacv.cpp.opencv_objdetect.CV_HAAR_FIND_BIGGEST_OBJECT;
import static com.googlecode.javacv.cpp.opencv_objdetect.cvHaarDetectObjects;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_objdetect;
import com.googlecode.javacv.cpp.opencv_objdetect.CvHaarClassifierCascade;

import edu.lipreading.Constants;
import edu.lipreading.Utils;

/**
 * This FeatureExtractor extracts lip coordinates automatically by identifying user lips.
 */
public class NoMoreStickersFeatureExtractor extends AbstractFeatureExtractor{
	private static final int RECT_VERTICAL_JUMP = 10;
	private static final int RECT_FRAME_THRESHOLD = 5;
	private final ExecutorService executor = Executors.newCachedThreadPool();
	private IplImage manipulated;
	private CvRect prev = new CvRect();
    private CvHaarClassifierCascade classifier;
	private CvMemStorage storage;
    private int sideConfidence;
	private int lowerConfidence;
	private int upperConfidence;
	private int rectFrameCount = 0;

	public List<Integer> getPoints(IplImage grabbed) throws Exception {
        int roiFix = grabbed.height() / -32;
		sideConfidence = grabbed.width() / 128;
		lowerConfidence = grabbed.height() / 128;
		upperConfidence = grabbed.width() / 32;

		if(manipulated == null)
			manipulated = cvCreateImage(cvSize(grabbed.width(), grabbed.height()), IPL_DEPTH_8U, 1);
		if(storage == null || classifier == null)
			init();
		cvClearMemStorage(storage);
		cvCvtColor(grabbed, manipulated, CV_RGB2GRAY);
		CvSeq mouths = cvHaarDetectObjects(manipulated, classifier, storage, 1.8, 13, CV_HAAR_FIND_BIGGEST_OBJECT);
		CvRect r = new CvRect(cvGetSeqElem(mouths, 0));
		if(r.isNull()){
			return null;
		} else if (prev.y() != 0 && rectFrameCount < RECT_FRAME_THRESHOLD){
			if (Math.abs(r.y() - prev.y()) > RECT_VERTICAL_JUMP){
				CvRect.memcpy(r, prev, prev.sizeof());
				rectFrameCount++;
			}
		} else if (rectFrameCount >= RECT_FRAME_THRESHOLD){
			rectFrameCount = 0;
		}
		CvRect.memcpy(prev, r, r.sizeof());
		r.y(r.y() + roiFix);
		final int x = r.x(), y = r.y();
		cvSetImageROI(grabbed, r);
		final CvMat mat = grabbed.asCvMat();

		List<Future<int[]>> points = new Vector<Future<int[]>>();
		final Future<double[][]> getH = executor.submit(new Callable<double[][]>() {
			@Override
			public double[][] call() throws Exception {
				return getH(mat);
			}
		});
		final Future<double[][]> getL = executor.submit(new Callable<double[][]>() {
			@Override
			public double[][] call() throws Exception {
				return getL(mat);
			}
		});
		final Future<double[][]> getLmini = executor.submit(new Callable<double[][]>() {
			@Override
			public double[][] call() throws Exception {
				return Lmini(getL.get());
			}
		});
		final Future<int[]> getRight = executor.submit(new Callable<int[]>() {
			@Override
			public int[] call() throws Exception {
				return getRight(getL.get(), getLmini.get());
			}
		});
		points.add(getRight);
		final Future<int[]> getLeft = executor.submit(new Callable<int[]>() {
			@Override
			public int[] call() throws Exception {
				return getLeft(getL.get(), getLmini.get());
			}
		});
		points.add(getLeft);
		final Future<int[]> getUpper = executor.submit(new Callable<int[]>() {
			@Override
			public int[] call() throws Exception {
				return getUpper(getL.get(), getH.get(), getRight.get()[0], getLeft.get()[0]);
			}

		});
		points.add(getUpper);
		final Future<int[]> getLower = executor.submit(new Callable<int[]>() {
			@Override
			public int[] call() throws Exception {
				return getLower(getL.get(), getUpper.get()[0]);
			}
		});
		points.add(getLower);

		List<Integer> frameCoordinates = new Vector<Integer>();

		for (Future<int[]> point : points) {
			int coordinateX = point.get()[0] + x, coordinateY = point.get()[1] + y;
			frameCoordinates.add(coordinateX);
			frameCoordinates.add(coordinateY);
		}
		cvResetImageROI(grabbed);
		//cvRectangle(grabbed, cvPoint(x, y), cvPoint(x+r.width(), y+r.height()), CvScalar.GREEN, 1, CV_AA, 0);
		return frameCoordinates;
	}


   /* public List<Integer> getPointsSingleThreaded(IplImage grabbed) throws Exception {
        int roiFix = grabbed.height() / -32;
		sideConfidence = grabbed.width() / 128;
		lowerConfidence = grabbed.height() / 128;
		upperConfidence = grabbed.width() / 32;

		if(manipulated == null)
			manipulated = cvCreateImage(cvSize(grabbed.width(), grabbed.height()), IPL_DEPTH_8U, 1);
		if(storage == null || classifier == null)
			init();
		cvClearMemStorage(storage);
		cvCvtColor(grabbed, manipulated, CV_RGB2GRAY);
		CvSeq mouths = cvHaarDetectObjects(manipulated, classifier, storage, 1.8, 13, CV_HAAR_FIND_BIGGEST_OBJECT);
		CvRect r = new CvRect(cvGetSeqElem(mouths, 0));
		if(r.isNull()){
			return null;
		} else if (prev.y() != 0 && rectFrameCount < RECT_FRAME_THRESHOLD){
			if (Math.abs(r.y() - prev.y()) > RECT_VERTICAL_JUMP){
				CvRect.memcpy(r, prev, prev.sizeof());
				rectFrameCount++;
			}
		} else if (rectFrameCount >= RECT_FRAME_THRESHOLD){
			rectFrameCount = 0;
		}
		CvRect.memcpy(prev, r, r.sizeof());
		r.y(r.y() + roiFix);
		final int x = r.x(), y = r.y();
		cvSetImageROI(grabbed, r);
		final CvMat mat = grabbed.asCvMat();

        List<Integer> frameCoordinates = new Vector<Integer>();
        frameCoordinates.add(r.x());
        frameCoordinates.add(r.x() + r.width());
        frameCoordinates.add(r.y());
        frameCoordinates.add(r.y() + r.height());
        return frameCoordinates;
        double[][] h = getH(mat);
        double[][] l = getL(mat);
        double[][] lmini = Lmini(l);

        int[] right = getRight(l, lmini);
        frameCoordinates.add(right[0] + x);
        frameCoordinates.add(right[1] + y);

        int[] left = getLeft(l, lmini);
        frameCoordinates.add(left[0] + x);
        frameCoordinates.add(left[1] + y);

        int[] upper = getUpper(l, h, right[0], left[0]);
        frameCoordinates.add(upper[0] + x);
        frameCoordinates.add(upper[1] + y);

        int[] lower = getLower(l, upper[0]);
        frameCoordinates.add(lower[0] + x);
        frameCoordinates.add(lower[1] + y);

		cvResetImageROI(grabbed);
		//cvRectangle(grabbed, cvPoint(x, y), cvPoint(x+r.width(), y+r.height()), CvScalar.GREEN, 1, CV_AA, 0);
		return frameCoordinates;
	}*/

	private void init() throws Exception {
		Loader.load(opencv_objdetect.class);
		String fileNameFromUrl = Utils.getFileNameFromUrl(Constants.HAAR_CASCADE_MOUTH_FILE);
		if(!new File(fileNameFromUrl).exists())
			Utils.get(Constants.HAAR_CASCADE_MOUTH_FILE);
		classifier = new CvHaarClassifierCascade(cvLoad(Utils.getFileNameFromUrl(fileNameFromUrl)));
		storage = CvMemStorage.create();
	}

	@Override
	public void paintCoordinates(IplImage grabbed,
			List<Integer> frameCoordinates) {
		if(frameCoordinates != null){
			for (int i = 0; i < frameCoordinates.size(); i += 2) {
				cvCircle(grabbed,
						new CvPoint(frameCoordinates.get(i),
								frameCoordinates.get(i + 1)),
								1, CvScalar.GREEN, 3, 0, 0);
			}
		}
	}

	protected int[] getLower(double[][] L, int centerLine) {
		RealMatrix matrixL = new Array2DRowRealMatrix(L);
		double[] column = matrixL.getColumn(centerLine);
		for (int i = column.length - 1; i >= 0; i--) {
			boolean found = true;
			for (int j = i; j > Math.max(i - lowerConfidence, 0) && found; j--) {
				found &= column[j] < column[j - 1];
			}
			if(found)
				return new int[] {centerLine, i};
		}
		return new int []{centerLine,column.length * 3 / 4};
	}


	protected int[] getUpper(double[][] L, double[][] h, int right, int left) {
		RealMatrix matrixL = new Array2DRowRealMatrix(L);
		RealMatrix matrixH = new Array2DRowRealMatrix(h);
		RealMatrix hMinusL = matrixH.subtract(matrixL);
		int centerLine = (int)Math.round((double)(right + left) / 2);
		double[] column = hMinusL.getColumn(centerLine);
		// find first getting up
		boolean found = false;
		int y = 0;
		for(int i = 0; i < column.length - 1 && !found; i++){
			if(column[i] <= column[i + 1]){
				y = i;
				found = true;
				for (int j = i + 2; j < Math.min(column.length, i + upperConfidence) && found; j++){
					found &= column[i] < column[j];
				}
			}
		}
		return new int[]{centerLine, y};
	}


	/**
	 * @param roi a matrix of the roi pixels arranged as BGR
	 * @return the Hue matrix
	 */
	 private double[][] getH(CvMat roi){
		double[][] h = new double[roi.rows()][roi.cols()];
		double max = Double.MIN_VALUE;
		for (int i = 0; i < roi.rows(); i++) {
			for (int j = 0; j < roi.cols(); j++) {
				double R = roi.get(i, j , 2), G = roi.get(i, j , 1);
				//h = R / (G + R)
						h[i][j] = R / (G + R);
				max = Math.max(max, h[i][j]);
			}
		}
		//scale values to be between 0 - 1
		for (int i = 0; i < roi.rows(); i++) {
			for (int j = 0; j < roi.cols(); j++) {
				h[i][j] /= max;
			}
		}
		return h;
	 }

	 /**
	  * @param roi a matrix of the roi pixels arranged as BGR
	  * @return the luminance matrix
	  */
	 private double[][] getL(CvMat roi){
		 double[][] L = new double[roi.rows()][roi.cols()];
		 double max = Double.MIN_VALUE;
		 for (int i = 0; i < roi.rows(); i++) {
			 for (int j = 0; j < roi.cols(); j++) {
				 //L = (R + R + B + G + G + G) / 6
				 double R = roi.get(i, j , 2), G = roi.get(i, j , 1), B = roi.get(i, j , 0);
				 L[i][j] = (R + R + B + G + G + G) / 6;
				 max = Math.max(max, L[i][j]);
			 }
		 }
		 //scale values to be between 0 - 1
		 for (int i = 0; i < roi.rows(); i++) {
			 for (int j = 0; j < roi.cols(); j++) {
				 L[i][j] /= max;
			 }
		 }
		 return L;
	 }


	 /**
	  * @param L the luminance matrix
	  * @return both the Lmini line in 0 index and in [1][0] the mean luminance of Lmini
	  */
	 private double[][] Lmini(double [][] L){
		 RealMatrix l = new Array2DRowRealMatrix(L);
		 double[][] Lmini = new double[2][L[0].length];
		 for (int i = 0; i < L[0].length; i++) {
			 double[] column = l.getColumn(i);
			 int minIndex = Utils.getMinIndex(column, false);
			 Lmini[0][i] = minIndex;
			 Lmini[1][0] += column[minIndex];
		 }
		 Lmini[1][0] /= L[0].length;
		 return Lmini;
	 }

	 private int[] getRight(double[][] L, double[][] Lmini){
		 final int[] ans = new int[2];
		 final double meanL = Lmini[1][0];
		 for (int i = Lmini[0].length - 1; i >= 0; i--) {
			 if(L[(int)Lmini[0][i]][i] < meanL){
				 boolean found = true;
				 for (int j = i; j > Math.max(i - sideConfidence, 0) && found; j--) {
					 found &= L[(int)Lmini[0][j]][j] < meanL;
				 }
				 if(found)
					 return new int[] {i, (int)Lmini[0][i]};
			 }
		 }
		 return ans;
	 }

	 private int[] getLeft(double[][] L, double[][] Lmini){
		 final int[] ans = new int[2];
		 final double meanL = Lmini[1][0];
		 for (int i = 0; i < Lmini[0].length; i++) {
			 if(L[(int)Lmini[0][i]][i] < meanL){
				 boolean found = true;
				 for (int j = i; j < Math.min(i + sideConfidence, Lmini[0].length) && found; j++) {
					 found &= L[(int)Lmini[0][j]][j] < meanL;
				 }
				 if(found)
					 return new int[] {i, (int)Lmini[0][i]};
			 }
		 }
		 return ans;
	 }

    public void setClassifier(CvHaarClassifierCascade classifier) {
        this.classifier = classifier;
    }

    public void setStorage(CvMemStorage storage) {
        this.storage = storage;
    }


	 public void shutdown(){
		 executor.shutdownNow();
		 if(storage != null)
			 storage.release();
	 }

	 public static void main(String ... args) throws Exception{
		/* NoMoreStickersFeatureExtractor fe = new NoMoreStickersFeatureExtractor();
		 //fe.setOutput(true);
		 fe.extract( null );
		 fe.shutdown();*/
     }

}