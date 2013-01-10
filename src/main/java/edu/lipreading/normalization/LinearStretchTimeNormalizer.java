package edu.lipreading.normalization;

import edu.lipreading.Sample;

public class LinearStretchTimeNormalizer implements Normalizer {

	/* 
	 * This normalizer should insert intermediate frames between existing frames
	 * such that if in between two frames we need to add n frames
	 * the i generated frame coordinate is (f1*(n-i)/(n) + f2*(1-(n-i)/(n))) / 2
	 */
	@Override
	public Sample normalize(Sample sample) {
		// TODO Auto-generated method stub
		return null;
	}

}
