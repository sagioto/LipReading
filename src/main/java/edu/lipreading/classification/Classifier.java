package edu.lipreading.classification;

import java.util.List;

import edu.lipreading.Sample;

public interface Classifier {
	public String classify(List<Sample> trainingSet, Sample test);
}
