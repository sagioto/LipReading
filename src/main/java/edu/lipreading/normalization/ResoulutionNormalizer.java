package edu.lipreading.normalization;

import java.util.List;

import edu.lipreading.Sample;

/**
 * This normalizer class intends to normalize the given Sample to a chosen resolution,
 * stretching or contracting the sample's points respectively.
 * @author Dagan
 *
 */
public class ResoulutionNormalizer implements Normalizer {

	public final static int NORMALIZED_WIDTH = 800;
	public final static int NORMALIZED_HEIGHT = 600;
	
	@Override
	public Sample normalize(Sample sample) {
		// TODO need to get the sample's current WIDTH and HEIGHT
		int sampleWidth = NORMALIZED_WIDTH;
		int sampleHeight = NORMALIZED_HEIGHT;
		
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
		return null; //TODO When Sample original resolution can be acquired this should be changed back to return the sample.
	}

}
