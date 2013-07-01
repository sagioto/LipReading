package edu.lipreading.classification;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.functions.MultilayerPerceptron;

import java.io.InputStream;

/**
 * This Classifier uses Weka Neural Network MultilayerPerceptron classifier to train and classify Samples.
 */
public class MultiLayerPerceptronClassifier extends WekaClassifier{

    public MultiLayerPerceptronClassifier(InputStream modelFileInputStream) throws Exception {
        super(modelFileInputStream);
    }

    public MultiLayerPerceptronClassifier(String modelFile) throws Exception {
        super(modelFile);
    }

    public MultiLayerPerceptronClassifier() throws Exception {}

    @Override
    protected AbstractClassifier getNewClassifierInstance() {
        return new MultilayerPerceptron();
    }

}
