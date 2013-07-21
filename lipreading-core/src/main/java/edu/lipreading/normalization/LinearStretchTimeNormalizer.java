package edu.lipreading.normalization;

import edu.lipreading.Constants;
import edu.lipreading.Sample;

import java.util.List;
import java.util.Vector;

/**
 * This normalizer normalizes sample to a constant length.
 * The length is defined by Constants.FRAMES_COUNT.
 * This normalization is necessary when working with Weka, as instances must have the same length.
 */
public class LinearStretchTimeNormalizer implements Normalizer {

    @Override
    public Sample normalize(Sample sample) {
        Sample stretched = new Sample(sample.getId());
        double frameRatio = (double)(Constants.FRAMES_COUNT - 2) / (sample.getMatrix().size() - 2);
        for (int i = 0; i < Constants.FRAMES_COUNT; i++) {
            stretched.getMatrix().add(null);
        }
        List<Integer> indices = new Vector<Integer>();
        for (int i = 0; i < sample.getMatrix().size(); i++) {
            int curr = Math.min(Constants.FRAMES_COUNT - 1 ,(int)(i * frameRatio));
            stretched.getMatrix().set(curr, sample.getMatrix().get(i));
            indices.add(curr);
        }
        for (int i = 0; i < indices.size() - 1; i++) {
            int diff = indices.get(i + 1) - indices.get(i);
            for (int j = 1; j < diff; j++) {
                double f1Coef = (double)(diff - j) / diff;
                double f2Coef = 1 - f1Coef;
                List<Integer> intermidiate = new Vector<Integer>();
                List<Integer> f1 = stretched.getMatrix().get(indices.get(i));
                List<Integer> f2 = stretched.getMatrix().get(indices.get(i + 1));
                for (int k = 0; k < f1.size(); k++) {
                    intermidiate.add(k, (int) ((f1.get(k) * f1Coef) + (f2.get(k) * f2Coef)));
                }
                stretched.getMatrix().set(indices.get(i) + j, intermidiate);
            }
        }
        sample.setMatrix(stretched.getMatrix());
        return sample;
    }

}
