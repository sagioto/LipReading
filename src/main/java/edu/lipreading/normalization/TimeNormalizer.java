package edu.lipreading.normalization;

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
public class TimeNormalizer implements Normalizer{

	private static final int MAX_FRAMES = 100;

	@Override
	public Sample normalize(Sample sample) {
		Sample stretchedSample = new Sample(sample.getId());
		int subframes = MAX_FRAMES / sample.getMatrix().size();
		for (int i = 0; i < sample.getMatrix().size(); i++) {
			for (int j = 0; j < subframes; j++) {
				stretchedSample.getMatrix().add(sample.getMatrix().get(i));
			}
		}
		int diff = MAX_FRAMES - stretchedSample.getMatrix().size(); 
		for (int i = 0; i < diff; i++) {
			stretchedSample.getMatrix().add(sample.getMatrix().get(sample.getMatrix().size() - 1));
		}
		return stretchedSample;
	}

}
