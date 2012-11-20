package edu.lipreading.normalization;

import edu.lipreading.Sample;

public class EmptyNomelizer implements Normelizer{

	@Override
	public Sample normelize(Sample sample) {
		return sample;
	}
	
}
