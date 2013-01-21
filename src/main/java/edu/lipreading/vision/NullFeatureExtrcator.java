package edu.lipreading.vision;

import java.util.List;

import com.googlecode.javacv.cpp.opencv_core.IplImage;



public class NullFeatureExtrcator extends AbstractFeatureExtractor{

	@Override
	public void paintCoordinates(IplImage grabbed,
			List<Integer> frameCoordinates) {}

	@Override
	public List<Integer> getPoints(IplImage grabbed) throws Exception {
		return null;
	}

}
