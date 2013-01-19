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

import org.apache.commons.math3.analysis.interpolation.BicubicSplineInterpolatingFunction;
import org.apache.commons.math3.analysis.interpolation.SmoothingPolynomialBicubicSplineInterpolator;
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
@SuppressWarnings(value = { "unused" })
public class NoMoreStickersFeatureExtractor extends AbstractFeatureExtractor{
	private static final int ROI_FIX = -20;
	private static final int SIDE_CONFIDENCE = 10;
	private static final int VERTICTAL_CONFIDENCE = 5;
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
				recorder.setFrameRate(grabber.getFrameRate());
				recorder.start();
			}
		}
		IplImage manipulated = cvCreateImage(cvSize(width, height), IPL_DEPTH_8U, 1);
		CvMemStorage storage = CvMemStorage.create();

		while((grabbed = grabber.grab()) != null){
			cvClearMemStorage(storage);
			cvCvtColor(grabbed, manipulated, CV_RGB2GRAY);
			CvSeq mouths = cvHaarDetectObjects(manipulated, classifier, storage, 1.8, 13, CV_HAAR_FIND_BIGGEST_OBJECT);
			CvRect r = new CvRect(cvGetSeqElem(mouths, 0));
			r.y(r.y() + ROI_FIX);
			final int x = r.x(), y = r.y();
			cvSetImageROI(grabbed, r);
			final CvMat mat = grabbed.asCvMat();

			List<Future<int[]>> points = new Vector<Future<int[]>>();
			/*final Future<double[][]> getH = executor.submit(new Callable<double[][]>() {
				@Override
				public double[][] call() throws Exception {
					return getH(mat);
				}
			});*/
			final Future<double[][]> getL = executor.submit(new Callable<double[][]>() {
				@Override
				public double[][] call() throws Exception {
					return getL(mat);
				}
			});
			/*TODO make gradient algorithm work
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
			points.add(getUpper);*/
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
			/*TODO make gradient algorithm work
			final Future<int[]> getLower = executor.submit(new Callable<int[]>() {
				@Override
				public int[] call() throws Exception {
					return getLower(getCenterLine(getRight, getLeft), getRinf.get());
				}
			});*/
			final Future<int[]> getUpper = executor.submit(new Callable<int[]>() {
				@Override
				public int[] call() throws Exception {
					return getUpper(getL.get(), getCenterLine(getRight, getLeft));
				}

			});
			points.add(getUpper);
			final Future<int[]> getLower = executor.submit(new Callable<int[]>() {
				@Override
				public int[] call() throws Exception {
					return getLower(getL.get(), getCenterLine(getRight, getLeft));
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
			if(isGui()){
				for (int i = 0; i < frameCoordinates.size(); i += 2) {
					cvCircle(grabbed, 
							new CvPoint(frameCoordinates.get(i),
									frameCoordinates.get(i + 1)), 
									1, CvScalar.RED, 3, 0, 0);
				}
				//cvRectangle(grabbed, cvPoint(x, y), cvPoint(x+w, y+h), CvScalar.RED, 1, CV_AA, 0);
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

	protected int[] getLower(double[][] L, int centerLine) {
		RealMatrix matrixL = new Array2DRowRealMatrix(L);
		double[] column = matrixL.getColumn(centerLine);
		for (int i = column.length - 1; i >= 0; i--) {
			boolean found = true;
			for (int j = i; j > Math.max(i - VERTICTAL_CONFIDENCE, 0) && found; j--) {
				found &= column[j] <= column[j - 1];
			}
			if(found)
				return new int[] {centerLine, i};
		}
		return new int []{centerLine, L.length * 3 / 4};
	}


	protected int[] getUpper(double[][] L, int centerLine) {
		RealMatrix matrixL = new Array2DRowRealMatrix(L);
		double[] column = matrixL.getColumn(centerLine);
		for (int i = 0; i < column.length; i++) {
			boolean found = true;
			for (int j = i; j < Math.min(i + VERTICTAL_CONFIDENCE, column.length) && found; j++) {
				found &= column[j] > column[j + 1];
			}
			if(found)
				return new int[] {centerLine, i};
		}
		return new int []{centerLine, L.length / 4};
		
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

	private double[][][] getRsup(double[][] h, double[][] L){
		double[][][] ans = new double[h.length][h[0].length][3];
		RealMatrix matrixL = new Array2DRowRealMatrix(L);
		RealMatrix matrixH = new Array2DRowRealMatrix(h);
		RealMatrix hMinusL = matrixH.subtract(matrixL);
		double [] xs = new double[hMinusL.getRowDimension()], 
				ys = new double[hMinusL.getColumnDimension()];
		for (int i = 0; i < xs.length; i++) {
			xs[i] = i;
		}
		for (int i = 0; i < ys.length; i++) {
			ys[i] = i;	
		}
		SmoothingPolynomialBicubicSplineInterpolator iterpolator = new SmoothingPolynomialBicubicSplineInterpolator();
		long start = System.currentTimeMillis(); 
		BicubicSplineInterpolatingFunction func = iterpolator.interpolate(xs, ys, hMinusL.getData());
		System.out.println("sup " + (System.currentTimeMillis() - start));
		for (int i = 0; i < ans.length; i++) {
			for (int j = 0; j < ans[0].length; j++) {
				ans[0][i][j] = func.partialDerivativeX(i, j);
				ans[1][i][j] = func.partialDerivativeY(i, j);
				ans[2][i][j] = func.partialDerivativeXY(i, j);
			}
		}
		return ans;
	}

	private double[][] getRinf(double[][] h, double[][] L){
		double[][] ans = new double[h.length][h[0].length];
		RealMatrix matrixL = new Array2DRowRealMatrix(L);
		RealMatrix matrixH = new Array2DRowRealMatrix(h);
		RealMatrix hPlusL = matrixH.add(matrixL);
		double [] xs = new double[hPlusL.getRowDimension()], 
				ys = new double[hPlusL.getColumnDimension()];
		for (int i = 0; i < xs.length; i++) {
			xs[i] = i;
		}
		for (int i = 0; i < ys.length; i++) {
			ys[i] = i;	
		}
		long start = System.currentTimeMillis(); 
		SmoothingPolynomialBicubicSplineInterpolator iterpolator = new SmoothingPolynomialBicubicSplineInterpolator();
		BicubicSplineInterpolatingFunction func = iterpolator.interpolate(xs, ys, hPlusL.getData());
		for (int i = 0; i < ans.length; i++) {
			for (int j = 0; j < ans[0].length; j++) {
				ans[i][j] = func.partialDerivativeY(i, j); 
			}
		}
		System.out.println("inf " + (System.currentTimeMillis() - start));
		return ans;
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
				for (int j = i; j > Math.max(i - SIDE_CONFIDENCE, 0) && found; j--) {
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
				for (int j = i; j < Math.min(i + SIDE_CONFIDENCE, Lmini[0].length) && found; j++) {
					found &= L[(int)Lmini[0][j]][j] < meanL;
				}
				if(found)
					return new int[] {i, (int)Lmini[0][i]};
			}
		}
		return ans;
	}

	private int[] getLower(int center, double[][] Rinf){
		int[] ans = new int[2];
		RealMatrix RinfMatrix = new Array2DRowRealMatrix(Rinf);
		ans[0] = center; 
		ans[1] = Utils.getMinIndex(RinfMatrix.getColumn(center));
		return ans;
	}

	private int[] getUpper(double[][][] Rsup, double[][] Rinf){
		int[] ans = new int[2];
		//TODO make gradient algorithm work
		return ans;
	}

	private int getCenterLine(final Future<int[]> getRight,
			final Future<int[]> getLeft)
					throws InterruptedException, ExecutionException {
		return (getRight.get()[0] + getLeft.get()[0]) / 2;
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
		NoMoreStickersFeatureExtractor fe = new NoMoreStickersFeatureExtractor();
		fe.setOutput(false);
		fe.extract("various.3gp");
		fe.shutdown();

	}

}