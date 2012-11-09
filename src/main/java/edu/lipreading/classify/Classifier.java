package edu.lipreading.classify;

import java.util.List;

import edu.lipreading.Point;

public interface Classifier {
	public String classify(List<List<Point>> data);
}
