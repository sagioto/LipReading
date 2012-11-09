package edu.lipreading.vision;

import java.util.List;
import java.util.Vector;

import com.googlecode.javacv.FrameGrabber.Exception;
import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import edu.lipreading.Point;

public class ColoredStickersFeatureExtractor extends AbstractFeatureExtractor{
	private final static short NUM_OF_STICKERS = 4;

	private final static short X_VECTOR_INDEX = 0;
	private final static short Y_VECTOR_INDEX = 1;
	private final static short COUNT_VECTOR_INDEX = 2;

	private final static short RED_VECTOR_INDEX = 0;
	private final static short GREEN_VECTOR_INDEX = 1;
	private final static short BLUE_VECTOR_INDEX = 2;
	private final static short YELLOW_VECTOR_INDEX = 3;


	private final static short[] RED_MIN = {0, 0, 130};
	private final static short[] RED_MAX = {80, 80, 255};

	private final static short[] GREEN_MIN = {0, 40, 0};
	private final static short[] GREEN_MAX = {50, 255, 50};

	private final static short[] BLUE_MIN = {150, 0, 0};
	private final static short[] BLUE_MAX = {255, 150, 150};

	private final static short[] YELLOW_MIN = {0, 120, 120};
	private final static short[] YELLOW_MAX = {90, 255, 255};

	private static int[][] summeries;
	private static int cols;
	private static int redCount = 0;
	private static int greenCount= 0;
	private static int blueCount= 0; 
	private static int yellowCount= 0; 

	@Override
	protected List<List<Point>> getPoints() throws Exception {
		List<List<Point>> allColorsVector = new Vector<List<Point>>(); 
		for(int i = 0; i < NUM_OF_STICKERS; i++){
			allColorsVector.add(new Vector<Point>());
		}
		IplImage grabbed;
		do{
			grabbed = grabber.grab();
			
			CvMat mat = grabbed.asCvMat();
			cols = mat.cols();
			double[] colorMatrix = mat.get();
			summeries = new int[NUM_OF_STICKERS][3];
			for (int i = 0; i < colorMatrix.length; i += 3) {
				if(isRed(colorMatrix[i], colorMatrix[i + 1], colorMatrix[i + 2])){
					storePoint(RED_VECTOR_INDEX, i);
				}
				if(isGreen(colorMatrix[i], colorMatrix[i + 1], colorMatrix[i + 2])){
					storePoint(GREEN_VECTOR_INDEX, i);
				}
				if(isBlue(colorMatrix[i], colorMatrix[i + 1], colorMatrix[i + 2])){
					storePoint(BLUE_VECTOR_INDEX, i);
				}
				if(isYellow(colorMatrix[i], colorMatrix[i + 1], colorMatrix[i + 2])){
					storePoint(YELLOW_VECTOR_INDEX, i);
				}
			}
			for(int i = 0; i < NUM_OF_STICKERS; i++){
				allColorsVector.get(i).add(new Point(getX(i), getY(i)));
			}
			
		}
		while(grabbed != null);
		
		return allColorsVector;
	}

	private short getX(int i) {
		return (short)(summeries[i][X_VECTOR_INDEX] / summeries[i][COUNT_VECTOR_INDEX]);
	}

	private short getY(int i) {
		return (short)(summeries[i][Y_VECTOR_INDEX] / summeries[i][COUNT_VECTOR_INDEX]);
	}

	private boolean isYellow(double r, double g, double b) {
		return isColor(r, g, b, YELLOW_MIN, YELLOW_MAX);
	}

	private boolean isBlue(double r, double g, double b) {
		return isColor(r, g, b, BLUE_MIN, BLUE_MAX);
	}

	private boolean isGreen(double r, double g, double b) {
		return isColor(r, g, b, GREEN_MIN, GREEN_MAX);
	}

	private boolean isRed(double r, double g, double b) {
		return isColor(r, g, b, RED_MIN, RED_MAX);
	}  

	private boolean isColor(double r, double g, double b, short[] min,
			short[] max) {
		return isHigher(r, g, b, min) && isLower(r, g, b, max);
	}

	private boolean isLower(double r, double g, double b, short[] max) {
		return (r <= max[RED_VECTOR_INDEX]) && (g <= max[GREEN_VECTOR_INDEX])
				&& (b <= max[BLUE_VECTOR_INDEX]); 
	}

	private boolean isHigher(double r, double g, double b, short[] min) {
		return (r >= min[RED_VECTOR_INDEX]) && (g >= min[GREEN_VECTOR_INDEX])
				&& (b >= min[BLUE_VECTOR_INDEX]); 
	}

	private void storePoint(short colorIndex, int i) {
		summeries[colorIndex][X_VECTOR_INDEX] = (i / 3) % cols ;
		summeries[colorIndex][Y_VECTOR_INDEX] = (i / 3) / cols;
		switch(colorIndex){
		case RED_VECTOR_INDEX:
			summeries[colorIndex][COUNT_VECTOR_INDEX] = redCount++;
			break;
		case GREEN_VECTOR_INDEX:
			summeries[colorIndex][COUNT_VECTOR_INDEX] = greenCount++;
			break;
		case BLUE_VECTOR_INDEX:
			summeries[colorIndex][COUNT_VECTOR_INDEX] = blueCount++;
			break;
		case YELLOW_VECTOR_INDEX:
			summeries[colorIndex][COUNT_VECTOR_INDEX] = yellowCount++;
			break;		
		}
	}


}
