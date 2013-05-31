package edu.lipreading.classification;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.functions.SMO;

import java.io.InputStream;

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
}
