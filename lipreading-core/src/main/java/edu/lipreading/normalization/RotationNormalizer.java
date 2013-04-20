package edu.lipreading.normalization;

import edu.lipreading.Sample;

import java.util.List;

/**
 * This class is designed to perform a linear transformation on a
 * Sample object so the Sample is aligned against X,Y axis. This
 * Normalizer assumes rotation is smaller than 45 degrees. It will only
 * transform the sample if the angle is larger than the rotation
 * threshold.
 * @author Dagan
 */
public class RotationNormalizer implements Normalizer {

	protected static final double ROTATION_THRESHOLD = Math.toRadians(15);

	@Override
	public Sample normalize(Sample sample) {

		// first calculate the angle (in Radians!).
		double angle = 0;

		int smallestX = 100000000;
		int largestX = -100000000;
		int smallestXIndex = 0;
		int largestXIndex = 0;
		for (int i = 0; i < sample.getMatrix().size(); i++) {
			// Looking for the left most point.
			List<Integer> vector = sample.getMatrix().get(i);
			int avg = getAverageX(vector);
			if (avg < smallestX) {
				smallestX = avg;
				smallestXIndex = i;
			}
			if (avg > largestX) {
				largestX = avg;
				largestXIndex = i;
			}
		}
		List<Integer> leftVector = sample.getMatrix().get(smallestXIndex);
		List<Integer> rightVector = sample.getMatrix().get(largestXIndex);

		int leftX = leftVector.get(0);
		int leftY = leftVector.get(1);
		int rightX = rightVector.get(0);
		int rightY = rightVector.get(1);
		
		double dy = rightY - leftY;
		double dx = rightX - leftX;
		
		//estimate the center point to perform rotation around.
		double centerX = leftX+(dx/2);
		double centerY = leftY+(dy/2);

		angle = (-1)*Math.atan2(dy, dx);

		if (Math.abs(angle) > ROTATION_THRESHOLD) {
			for (List<Integer> vector : sample.getMatrix()) {
				for (int i = 0; i < vector.size(); i += 2) {
					double x = vector.get(i)-centerX; //rotation around (0,0)
					double y = vector.get(i + 1)-centerY; //rotation around (0,0)
					// x' = x*cos(a) - y*sin(a)
					// y' = x*sin(a) + y*cos(a)
					vector.set(i,
							(int)(Math.round(((x * Math.cos(angle)) - (y * Math.sin(angle))))+centerX));
					vector.set(i + 1,
							(int)(Math.round(((x * Math.sin(angle)) + (y * Math.cos(angle))))+centerY));
				}
			}
		}

		return sample;
	}

	private int getAverageX(List<Integer> vector) {
		int sum = 0;
		for (int i = 0; i < vector.size(); i += 2) {
			sum += vector.get(i);
		}
		return (sum / (vector.size() / 2));
	}

}
