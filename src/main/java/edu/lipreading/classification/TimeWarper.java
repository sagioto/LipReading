/**
 * 
 */
package edu.lipreading.classification;

import java.util.List;

import edu.lipreading.Point;
import edu.lipreading.Sample;

/**
 * @author Dor
 *
 */
public class TimeWarper {
	private final static short NUM_OF_STICKERS = 4;

	public TimeWarper() {
		// TODO Auto-generated constructor stub
	}
	
	
	public double dtw(Sample test, Sample training) {
		double cost = 0;

		double[][] dtwMatrix = new double[training.getMatrix().size()][test.getMatrix().size()];

		// Initialize first row & first column:
		for( int i = 0; i < dtwMatrix.length; i++ ) 
		{
			dtwMatrix[0][i] = Double.POSITIVE_INFINITY;
		}

		for( int i = 0; i < dtwMatrix[0].length; i++ ) 
		{
			dtwMatrix[i][0] = Double.POSITIVE_INFINITY;
		}

		dtwMatrix[0][0] = 0.0;

		// BUild the matrix:
		for( int i = 1; i < dtwMatrix.length; i++ ) 
		{
			for( int j = 1; j < dtwMatrix[0].length; j++ ) 
			{
				cost =	dist2(test.getMatrix().get(j), training.getMatrix().get(i));
				dtwMatrix[i][j] = cost + min( dtwMatrix[i-1][j], dtwMatrix[i][j-1], dtwMatrix[i-1][j-1] );
			}
		}

		// Return last cell of Matrix (shortest path):
		return dtwMatrix[dtwMatrix.length-1][dtwMatrix[0].length-1];
	}

	
	private double min(double a, double b, double c) {
	      return Math.min(Math.min(a, b), c);
	}
	
	
	private double dist2(List<Point> u, List<Point> v) {

	  double outcome = 0.0;
	  
	  for (int i=0; i< NUM_OF_STICKERS; i++){
		  if ((u.get(i).getX() !=0 && u.get(i).getY()!=0)){ // Ignore points that weren't recognized
			  outcome += Math.pow((u.get(i).getX() - v.get(i).getX()), 2) + Math.pow((u.get(i).getY() - v.get(i).getY()), 2);
		  }
	  }
	  
	  return outcome;
	}
	
	
	
}
