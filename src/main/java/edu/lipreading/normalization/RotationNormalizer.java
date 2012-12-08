package edu.lipreading.normalization;

import java.util.List;

import edu.lipreading.Sample;

/**
 * @author Dagan This class is designed to perform a linear transformation on a
 *         Sample object so the Sample is aligned against X,Y axis. This
 *         Normalizer assumes rotation is smaller than 45 degrees.
 *         It will only transform the sample if the angle is larger than the rotation threshold.
 */
public class RotationNormalizer implements Normalizer {

	protected static final double ROTATION_THRESHOLD = Math.toRadians(15);

	@Override
	public Sample normalize(Sample sample) {
		
		//first calculate the angle (in Radians!).
		double angle = 0;

		int smallestX = 100000000;
		int smallestXIndex = 0;
		for (int i = 0; i < sample.getMatrix().size(); i++) {
			//Looking for the left most point.
			List<Integer> vector = sample.getMatrix().get(i);
			int avg = getAverageX(vector);
			if (avg < smallestX) {
				smallestX = avg;
				smallestXIndex = i;
			}
		}
		List<Integer> leftVector = sample.getMatrix().get(smallestXIndex);
		//assuming the first point should be aligned to (?,0).
		int leftX = leftVector.get(0);
		int leftY = leftVector.get(1);
		double z = Math.sqrt(Math.pow(leftX, 2) + Math.pow(leftY, 2));
		angle = Math.asin(leftY/z);
		
		if (Math.abs(angle) > ROTATION_THRESHOLD) {
			for (List<Integer> vector : sample.getMatrix()) {
				for (int i = 0; i < vector.size(); i += 2) {
					int x = vector.get(i);
					int y = vector.get(i + 1);
					// x' = x*cos(a) - y*sin(a)
					// y' = x*sin(a) + y*cos(a)
					vector.set(i,
							(int) (x * Math.cos(angle) - y * Math.sin(angle)));
					vector.set(i + 1,
							(int) (x * Math.sin(angle) + y * Math.cos(angle)));
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
