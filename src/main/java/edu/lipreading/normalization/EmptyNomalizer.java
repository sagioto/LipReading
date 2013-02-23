package edu.lipreading.normalization;

import edu.lipreading.Sample;

public class EmptyNomalizer implements Normalizer{

	@Override
	public Sample normalize(Sample sample) {
		return sample;
	}
	
}
