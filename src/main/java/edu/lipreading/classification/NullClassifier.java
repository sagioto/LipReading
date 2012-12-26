package edu.lipreading.classification;

import java.util.List;

import edu.lipreading.Sample;

public class NullClassifier implements Classifier {

	@Override
	public String test(Sample test) {
		return null;
	}

	@Override
	public void train(List<Sample> trainingSet) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(Sample train) {
		// TODO Auto-generated method stub
		
	}

}
