package edu.lipreading.normalization;

import edu.lipreading.Sample;

import java.util.List;

/**
 * This normalizer class intends to normalize the given Sample coordinates to be around (0,0).
 * The center point of Sample`s lip coordinates will be (0,0) after normalization.
 *
 */
public class CenterNormalizer implements Normalizer{

    protected static final int X_INDEX = 0;
    protected static final int Y_INDEX = 1;

    @Override
    public Sample normalize(Sample sample) {
        for (List<Integer> vector : sample.getMatrix()) {
            int[] center = getCenter(vector);
            for (int i = 0; i < vector.size(); i++) {
                if(i % 2 == 0)
                    vector.set(i, vector.get(i)- center[X_INDEX]);
                else
                    vector.set(i, vector.get(i)- center[Y_INDEX]);
            }
        }
        return sample;
    }

    protected int[] getCenter(List<Integer> vector) {
        int[] center = {0,0};
        for (int i = 0; i < vector.size(); i++) {
            if(i % 2 == 0)
                center[X_INDEX] += vector.get(i);
            else
                center[Y_INDEX] += vector.get(i);
        }
        center[X_INDEX] = (int)Math.round(((double)center[X_INDEX]) / (vector.size() / 2));
        center[Y_INDEX] = (int)Math.round(((double)center[Y_INDEX]) / (vector.size() / 2));
        return center;
    }

}
