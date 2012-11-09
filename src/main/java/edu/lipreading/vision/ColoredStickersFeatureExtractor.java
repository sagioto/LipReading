package edu.lipreading.vision;

import static com.googlecode.javacv.cpp.opencv_core.cvScalar;

import java.util.List;

import com.googlecode.javacv.cpp.opencv_core.CvScalar;

import edu.lipreading.Point;

public class ColoredStickersFeatureExtractor extends AbstractFeatureExtractor{
	private final static CvScalar red_min = cvScalar(0, 0, 130, 0);
    private final static CvScalar red_max = cvScalar(80, 80, 255, 0);
    
    private final static CvScalar green_min = cvScalar(0, 40, 0, 0);
    private final static CvScalar green_max = cvScalar(50, 255, 50, 0);

    private final static CvScalar blue_min = cvScalar(150, 0, 0, 0);
    private final static CvScalar blue_max = cvScalar(255, 150, 150, 0);
    
    private final static CvScalar yellow_min = cvScalar(0, 120, 120, 0);
    private final static CvScalar yellow_max = cvScalar(90, 255, 255, 0);
	
    @Override
	protected List<List<Point>> getPoints() {
		// TODO Auto-generated method stub
		return null;
	}  

}
