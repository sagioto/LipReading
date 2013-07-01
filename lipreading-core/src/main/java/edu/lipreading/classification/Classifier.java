package edu.lipreading.classification;

import edu.lipreading.Sample;

import java.util.List;

/**
 * Classifier holds a training set, can add Samples to training set or
 * test a Sample and return its label.
 */
public interface Classifier {

    /**
     * Gets test Sample and return the label matches to this Sample
     * @param test
     * @return label of given Sample
     * @precondition Classifier is initialized and has being trained
     */
	public String test(Sample test);

    /**
     * Gets List of Samples and train the model/algorithm according to this set
     * @param trainingSet
     * @postcondition Classifier has trained model or training set containing given data
     */
	public void train(List<Sample> trainingSet);

    /**
     * Gets Sample and adds it to classification model/trainingSet
     * @param train
     * @precondition Classifier has trained model/trainingSet that does`nt contain given Sample
     * @postcondition Classifier trained model/trainingSet contains given Sample data
     */
	public void update(Sample train);

}
