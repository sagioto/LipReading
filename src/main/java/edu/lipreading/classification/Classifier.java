package edu.lipreading.classification;

import java.util.List;

import edu.lipreading.Sample;

public interface Classifier {
	public String test(Sample test);
	
	public void train(List<Sample> trainingSet);
	
	public void update(Sample train);
}
