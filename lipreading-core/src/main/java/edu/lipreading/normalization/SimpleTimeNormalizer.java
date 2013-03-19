package edu.lipreading.normalization;

import edu.lipreading.Constants;
import edu.lipreading.Sample;

/**
 * This class assumes number of frames is no more than <code>MAX_FRAMES</code>
 * we add sub-frames by duplicating each frame by the ratio between <code>MAX_FRAMES</code>
 * and the actual sample frame length. We than duplicate the last frame as many time as needed to
 * a stretched sample with <code>MAX_FRAMES</code> frames.
 * 
 * @author Sagi Bernstein
 *
 */
public class SimpleTimeNormalizer implements Normalizer{

	private static final int MAX_FRAMES = Constants.FRAMES_COUNT;

	@Override
	public Sample normalize(Sample sample) {
		Sample stretchedSample = new Sample(sample.getId());
		int originalSize = sample.getMatrix().size();
		int subframes = MAX_FRAMES / originalSize;
		for (int i = 0; i < originalSize; i++) {
			for (int j = 0; j < subframes; j++) {
				stretchedSample.getMatrix().add(sample.getMatrix().get(i));
			}
		}
		int diff = MAX_FRAMES - stretchedSample.getMatrix().size(); 
		for (int i = 0; i < diff; i++) {
			stretchedSample.getMatrix().add(sample.getMatrix().get(originalSize - 1));
		}
		return stretchedSample;
	}

}
