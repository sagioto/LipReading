package edu.lipreading.classification;

import edu.lipreading.Sample;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.functions.SMO;
import weka.core.Instance;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.*;

/**
 * Created with IntelliJ IDEA.
 * User: Dagan
 * Date: 24/05/13
 * Time: 20:13
 * To change this template use File | Settings | File Templates.
 */
public class SVMClassifier extends WekaClassifier {

    public SVMClassifier(InputStream modelFileInputStream) throws Exception {
        super(modelFileInputStream);
    }

    public SVMClassifier(String modelFile) throws Exception {
        super(modelFile);
    }

    @Override
    protected AbstractClassifier getNewClassifierInstance() {
        return new SMO();
    }

    @Override
    public String test(Sample test) {
        double ans = -1;
        try {
            Instance sampleToInstance = sampleToInstance(test);

            double[] dists = classifier.distributionForInstance(sampleToInstance);
            ans = getMax(dists);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ((SMO)classifier).classAttributeNames()[((int) ans)];
        //return vocabulary.get((int) ans);
    }

    private int getMax(double[] dists) {
        int max = 0;
        double maxVal = Double.MIN_VALUE;
        for(int i=0; i<dists.length; i++) {
            if(dists[i] > maxVal) {
                maxVal = dists[i];
                max=i;
            }
        }
        return max;
    }
}
