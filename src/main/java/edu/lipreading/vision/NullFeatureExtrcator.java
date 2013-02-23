package edu.lipreading.vision;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

import java.util.List;



public class NullFeatureExtrcator extends AbstractFeatureExtractor{

	@Override
	public void paintCoordinates(IplImage grabbed,
			List<Integer> frameCoordinates) {}

	@Override
	public List<Integer> getPoints(IplImage grabbed) throws Exception {
		return null;
	}

}
