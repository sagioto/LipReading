package edu.lipreading.vision;

import com.googlecode.javacv.FrameGrabber.Exception;

import edu.lipreading.Sample;

public class NullFeatureExtrcator extends AbstractFeatureExtractor{

	@Override
	protected Sample getPoints() throws Exception {
		return null;
	}

}
