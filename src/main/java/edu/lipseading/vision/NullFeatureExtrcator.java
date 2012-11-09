package edu.lipseading.vision;

import java.util.List;

import edu.lipreading.Point;

public class NullFeatureExtrcator extends AbstractFeatureExtractor{

	@Override
	protected List<List<Point>> getPoints() {
		return null;
	}

}
