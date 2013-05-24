package edu.lipreading.normalization;

import edu.lipreading.Sample;
import edu.lipreading.Utils;

import java.awt.*;
import java.util.List;

/**
 * This normalizer class intends to normalize the given Sample coordinates to be around (0,0).
 * The center point of Sample`s lip coordinates will be (0,0) after normalization.
 *
 */
public class CenterNormalizer implements Normalizer{



    @Override
    public Sample normalize(Sample sample) {

        for (int i=0; i< sample.getMatrix().size(); i++) {
            List<Integer> vector = sample.getMatrix().get(i);
            int[] center = Utils.getCenter(vector);

            if (i==0 && sample.getLeftEye() != null && sample.getRightEye() != null){ //If first frame - normalize also eyes
                sample.setLeftEye(new Point((int)sample.getLeftEye().getX() - center[Utils.X_INDEX], (int)sample.getLeftEye().getY() - center[Utils.Y_INDEX]));
                sample.setRightEye(new Point((int)sample.getRightEye().getX() - center[Utils.X_INDEX], (int)sample.getRightEye().getY() - center[Utils.Y_INDEX]));
            }
            for (int j = 0; j < vector.size(); j++) {
                 if(j % 2 == 0)
                    vector.set(j, vector.get(j)- center[Utils.X_INDEX]);
                else
                    vector.set(j, vector.get(j)- center[Utils.Y_INDEX]);
            }
        }
        return sample;
    }


}
