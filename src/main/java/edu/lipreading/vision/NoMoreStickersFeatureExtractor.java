package edu.lipreading.vision;

import static com.googlecode.javacv.cpp.opencv_core.CV_AA;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvCircle;
import static com.googlecode.javacv.cpp.opencv_core.cvClearMemStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSeqElem;
import static com.googlecode.javacv.cpp.opencv_core.cvLoad;
import static com.googlecode.javacv.cpp.opencv_core.cvPoint;
import static com.googlecode.javacv.cpp.opencv_core.cvRectangle;
import static com.googlecode.javacv.cpp.opencv_core.cvResetImageROI;
import static com.googlecode.javacv.cpp.opencv_core.cvSetImageROI;
import static com.googlecode.javacv.cpp.opencv_core.cvSize;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_RGB2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static com.googlecode.javacv.cpp.opencv_objdetect.CV_HAAR_FIND_BIGGEST_OBJECT;
import static com.googlecode.javacv.cpp.opencv_objdetect.cvHaarDetectObjects;

import java.io.File;
import java.io.PrintStream;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.FFmpegFrameRecorder;
import com.googlecode.javacv.FrameRecorder;
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
import edu.lipreading.Sample;
import edu.lipreading.Utils;

public class NoMoreStickersFeatureExtractor extends AbstractFeatureExtractor{
	private final ExecutorService executor = Executors.newCachedThreadPool();

	@Override
	protected Sample getPoints() throws Exception, InterruptedException,
	ExecutionException {
		IplImage grabbed;
		CanvasFrame frame = null;
		FrameRecorder recorder = null;
		Loader.load(opencv_objdetect.class);
		String fileNameFromUrl = Utils.getFileNameFromUrl(Constants.HAAR_CASCADE_MOUTH_FILE);
		if(!new File(fileNameFromUrl).exists())
			Utils.get(Constants.HAAR_CASCADE_MOUTH_FILE);
		CvHaarClassifierCascade classifier = new CvHaarClassifierCascade(cvLoad(Utils.getFileNameFromUrl(fileNameFromUrl)));

		int width = grabber.getImageWidth();
		int height = grabber.getImageHeight();
		if(isGui()){
			frame = new CanvasFrame(getSample().getId(), CanvasFrame.getDefaultGamma()/grabber.getGamma());
			frame.setDefaultCloseOperation(CanvasFrame.EXIT_ON_CLOSE);
			if(isOutput()){
				recorder = FFmpegFrameRecorder.createDefault(getSample().getId().split("\\.")[0] + "-output.MOV",width, height);
				recorder.start();
			}
		}
		IplImage manipulated = cvCreateImage(cvSize(width, height), IPL_DEPTH_8U, 1);
		CvMemStorage storage = CvMemStorage.create();

		while((grabbed = grabber.grab()) != null){
			cvClearMemStorage(storage);
			cvCvtColor(grabbed, manipulated, CV_RGB2GRAY);
			CvSeq mouths = cvHaarDetectObjects(manipulated, classifier, storage, 1.8, 1, CV_HAAR_FIND_BIGGEST_OBJECT);
			CvRect r = new CvRect(cvGetSeqElem(mouths, 0));
			r.y(r.y() - 20);
			final int x = r.x(), y = r.y(), w = r.width(), h = r.height();
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
			final Future<double[][][]> getRsup = executor.submit(new Callable<double[][][]>() {
				@Override
				public double[][][] call() throws Exception {
					return getRsup(getH.get(), getL.get());
				}
			});
			final Future<double[][]> getRinf = executor.submit(new Callable<double[][]>() {
				@Override
				public double[][] call() throws Exception {
					return getRinf(getH.get(), getL.get());
				}
			});
			final Future<int[]> getUpper = executor.submit(new Callable<int[]>() {
				@Override
				public int[] call() throws Exception {
					return getUpper(getRsup.get(), getRinf.get());
				}
			});
			points.add(getUpper);
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
			final Future<int[]> getLower = executor.submit(new Callable<int[]>() {
				@Override
				public int[] call() throws Exception {
					return getLower(getLeft.get(), getRight.get(), getRinf.get());
				}
			});
			points.add(getLower);

			List<Integer> frameCoordinates = new Vector<Integer>();

			for (Future<int[]> point : points) {
				int coordinateX = point.get()[0], coordinateY = point.get()[1];
				frameCoordinates.add(coordinateX);
				frameCoordinates.add(coordinateY);
			}
			cvResetImageROI(grabbed);
			if(isGui()){
				for (int i = 0; i < frameCoordinates.size(); i += 2) {
					cvCircle(grabbed, 
							new CvPoint(frameCoordinates.get(i) + x,
									frameCoordinates.get(i + 1) + y), 
									10, CvScalar.RED, 3, 0, 0);
				}
				cvRectangle(grabbed, cvPoint(x, y), cvPoint(x+w, y+h), CvScalar.RED, 1, CV_AA, 0);
				frame.showImage(grabbed);
				if(isOutput()){
					recorder.record(grabbed);
				}
			}
			
			getSample().getMatrix().add(frameCoordinates);
		}
		storage.release();
		if(isGui()){
			frame.dispose();
			if(isOutput()){
				recorder.stop();
			}
		}
		return getSample();
	}

	/**
	 * @param roi a matrix of the roi pixels arranged as BGR
	 * @return the Hue matrix
	 */
	private double[][] getH(CvMat roi){
		double[][] h = new double[roi.rows()][roi.cols()];
		for (int i = 0; i < roi.rows(); i++) {
			for (int j = 0; j < roi.cols(); j++) {
				double R = roi.get(i, j , 2), G = roi.get(i, j , 1);
				//h = R / (G + R)
				h[i][j] = R / (G + R);  
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

	private double[][][] getRsup(double[][] h, double[][] L){
		double[][][] ans = new double[h.length][h[0].length][3];
		double[][] derive = new double[h.length][h[0].length];
		for (int i = 0; i < h.length; i++) {
			for (int j = 0; j < h[0].length; j++) {
				derive[i][j] = h[i][j] - L[i][j];
			}			
		}
		//TODO
		return ans;
	}

	private double[][] getRinf(double[][] h, double[][] L){
		double[][] ans = new double[h.length][h[0].length];
		double[][] derive = new double[h.length][h[0].length];
		for (int i = 0; i < h.length; i++) {
			for (int j = 0; j < h[0].length; j++) {
				derive[i][j] = h[i][j] + L[i][j];
			}			
		}
		//TODO
		return ans;
	}


	/**
	 * @param L the luminance matrix
	 * @return both the Lmini line in 0 index and in [1][0] the mean luminance of Lmini
	 */
	private double[][] Lmini(double [][] L){
		//TODO this implementation is not working
		RealMatrix l = new Array2DRowRealMatrix(L);
		double[][] Lmini = new double[2][L[0].length];
		for (int i = 0; i < L[0].length; i++) {
			double[] column = l.getColumn(i);
			int minIndex = Utils.getMinIndex(column);
			Lmini[0][i] = minIndex; 
			Lmini[1][0] += column[minIndex];
		}
		Lmini[1][0] /= L[0].length;
		return Lmini;
	}

	private int[] getRight(double[][] L, double[][] Lmini){
		int[] ans = new int[2];
		//TODO
		return ans;
	}

	private int[] getLeft(double[][] L, double[][] Lmini){
		int[] ans = new int[2];
		for (int i = 0; i < Lmini[0].length; i++) {
			boolean found = true;
			for (int j = i; j < Math.min(i + 5, Lmini[0].length); j++) {
				found &= Lmini[0][j] < Lmini[1][0];
			}
			if(found)
				return new int[] {i, (int)Lmini[0][i]};
		}
		return ans;
	}

	private int[] getLower(int[] left, int[] right, double[][] Rinf){
		int[] ans = new int[2];
		//TODO
		return ans;
	}

	private int[] getUpper(double[][][] Rsup, double[][] Rinf){
		int[] ans = new int[2];
		//TODO
		return ans;
	}


	public static void printMatrix(double [][] matrix, PrintStream out){
		out.println(matrix.length + " " + matrix[0].length);
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				out.print(matrix[i][j]);
				if(j != matrix[i].length)
					out.print(" ");
			}
			out.println();
		}
	}
	
	public void shutdown(){
		this.executor.shutdownNow();
	}

	public static void main(String ... args) throws Exception{
		NoMoreStickersFeatureExtractor noMoreStickersFeatureExtractor = new NoMoreStickersFeatureExtractor();
		noMoreStickersFeatureExtractor.extract("yes.3gp");
		noMoreStickersFeatureExtractor.shutdown();
		/*double x[] = { -1, 0, 1.0, 2.0, 3.0, 5};
		double y[] = { 1.0, 0, 1.0, 4.0, 9.0, 25};
		UnivariateInterpolator interpolator = new SplineInterpolator();
		UnivariateFunction function = interpolator.interpolate(x, y);
		double interpolationX = 4;
		double value = function.value(interpolationX);
		System.out.println("f(" + interpolationX + ") = " + value);
		FiniteDifferencesDifferentiator d = new FiniteDifferencesDifferentiator(6, 0.000001);
		UnivariateDifferentiableFunction differentiate = d.differentiate(function);
		double x2 = 0.13;
		double value2 = differentiate.value(x2);
		System.out.println("f'(" + x2 + ") = " + value2);*/
	}

}