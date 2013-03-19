package edu.lipreading.classification;

import edu.lipreading.Sample;

import java.util.List;

public class NullClassifier implements Classifier {

	@Override
	public String test(Sample test) {
		return null;
	}

	@Override
	public void train(List<Sample> trainingSet) {}

	@Override
	public void update(Sample train) {}

}
