package edu.lipreading.classification;

import java.util.List;

import weka.classifiers.functions.MultilayerPerceptron;

import edu.lipreading.Sample;

public class MultilayerPerceptronClassifier implements Classifier{

	@Override
	public String classify(List<Sample> trainingSet, Sample test) {
		MultilayerPerceptron.main("-l nn2.model -T test.arff".split(" "));
		return null;
	}

}
