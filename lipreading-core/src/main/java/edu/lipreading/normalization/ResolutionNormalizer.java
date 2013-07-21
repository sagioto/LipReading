package edu.lipreading.normalization;

import edu.lipreading.Sample;

import java.util.List;

/**
 * This normalizer class intends to normalize the given Sample to a chosen resolution,
 * stretching or contracting the sample's points respectively.
 * @author Dagan
 *
 */
public class ResolutionNormalizer implements Normalizer {

	public final static int NORMALIZED_WIDTH = 640;
	public final static int NORMALIZED_HEIGHT = 480;
	
	@Override
	public Sample normalize(Sample sample) {
		int sampleWidth = sample.getWidth();
		int sampleHeight = sample.getHeight();
		
		double xStretchFactor = NORMALIZED_WIDTH/sampleWidth; 
		double yStretchFactor = NORMALIZED_HEIGHT/sampleHeight; 
		
		for(int i=0; i<sample.getMatrix().size(); i++) {
			List<Integer> vector = sample.getMatrix().get(i);
			for(int j=0; j<vector.size(); j++) {
				if(j % 2 == 0) {
					//x coordinate
					vector.set(j, (int) Math.round(vector.get(j)*xStretchFactor));
				} else {
					//y coordinate
					vector.set(j, (int) Math.round(vector.get(j)*yStretchFactor));
				}
			}
		}
		return sample;
	}

}
