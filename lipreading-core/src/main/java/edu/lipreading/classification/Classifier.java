package edu.lipreading.classification;

import edu.lipreading.Sample;

import java.util.List;

public interface Classifier {
	
	public String test(Sample test);

	public void train(List<Sample> trainingSet);

	public void update(Sample train);
	
}
