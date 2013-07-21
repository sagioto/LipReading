package edu.lipreading.normalization;

import edu.lipreading.Sample;

import java.util.List;
import java.util.Vector;

/**
 * This normalizer will normalize samples that have 'missing' frames.
 * Missing frames are actually frames with '0' values,
 * that are sometimes caused when recognition performance is poor and some points on the lips aren't recognized properly.
 * The normalizer uses simple linear interpolation technique to estimate the correct missing values.
 */
public class SkippedFramesNormalizer implements Normalizer {

	@Override
	public Sample normalize(Sample sample) {
        //TODO: handle null on first last or till the end and from beginning
        for (int i = 0; i < sample.getMatrix().size(); i++) {
            if(sample.getMatrix().get(i) == null){
                for (int j = i + 1; j < sample.getMatrix().size(); j++) {
                    if(sample.getMatrix().get(j) != null){
                            putIntermidiates(i - 1, j, sample);
                    }
                }
            }
        }
        return sample;
	}

    private void putIntermidiates(int startIndex, int stopIndex, Sample sample){
        int range = (stopIndex - startIndex);
        List<Integer> startFrame = sample.getMatrix().get(startIndex);
        List<Integer> stopFrame = sample.getMatrix().get(stopIndex);
        for (int i = 0; i < range - 1; i++) {
            List<Integer> points = new Vector<Integer>();
            double f1Coef = (double)(range - i) / range;
            double f2Coef = 1 - f1Coef;
            for (int j = 0; j < startFrame.size(); j++) {
                points.add(j, (int) ((startFrame.get(j) * f1Coef) + (stopFrame.get(j) * f2Coef)));
            }
            sample.getMatrix().add(startIndex + 1 + i, points);
        }
    }
}
