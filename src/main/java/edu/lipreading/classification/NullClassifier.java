package edu.lipreading.classification;

import java.util.List;

import edu.lipreading.Sample;

public class NullClassifier implements Classifier {

	@Override
	public String classify(List<Sample> training, Sample test) {
		return null;
	}

}
