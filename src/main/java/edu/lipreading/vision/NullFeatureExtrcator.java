package edu.lipreading.vision;

import java.util.List;

import com.googlecode.javacv.FrameGrabber.Exception;

import edu.lipreading.Point;

public class NullFeatureExtrcator extends AbstractFeatureExtractor{

	@Override
	protected List<List<Point>> getPoints() throws Exception {
		return null;
	}

}
