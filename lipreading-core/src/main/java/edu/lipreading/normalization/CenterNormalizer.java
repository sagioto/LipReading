package edu.lipreading.normalization;

import edu.lipreading.Sample;
import edu.lipreading.Utils;

import java.util.List;

/**
 * This normalizer class intends to normalize the given Sample coordinates to be around (0,0).
 * The center point of Sample`s lip coordinates will be (0,0) after normalization.
 *
 */
public class CenterNormalizer implements Normalizer{



    @Override
    public Sample normalize(Sample sample) {
        for (List<Integer> vector : sample.getMatrix()) {
            int[] center = Utils.getCenter(vector);
            for (int i = 0; i < vector.size(); i++) {
                 if(i % 2 == 0)
                    vector.set(i, vector.get(i)- center[Utils.X_INDEX]);
                else
                    vector.set(i, vector.get(i)- center[Utils.Y_INDEX]);
            }
        }
        return sample;
    }


}
