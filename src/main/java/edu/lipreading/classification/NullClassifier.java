package edu.lipreading.classification;

import java.util.List;

import edu.lipreading.Point;

public class NullClassifier implements Classifier {

	@Override
	public String classify(List<List<Point>> data) {
		return null;
	}

}
