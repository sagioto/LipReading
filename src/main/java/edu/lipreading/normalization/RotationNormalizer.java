package edu.lipreading.normalization;

import java.util.List;

import edu.lipreading.Sample;

public class RotationNormalizer implements Normalizer {

	protected static final int ROTATION_THRESHHOLD = 15;
	
	@Override
	public Sample normalize(Sample sample) {
		
		//TODO first calculate the angle. use an angle of 0 for now.
		int angle = 0;
		
		if(angle > ROTATION_THRESHHOLD) {
			for(List<Integer> vector : sample.getMatrix()) {
				for(int i=0; i<vector.size(); i+=2) {
					int x = vector.get(i);
					int y = vector.get(i+1);
					// x' = x*cos(a) - y*sin(a)
					// y' = x*sin(a) + y*cos(a)
					vector.set(i, (int)(x*Math.cos(angle) - y*Math.sin(angle)));
					vector.set(i+1, (int)(x*Math.sin(angle) + y*Math.cos(angle)));
				}
			}
		}
		
		return sample;
	}

}
