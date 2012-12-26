package edu.lipreading.vision;

import static com.googlecode.javacv.cpp.opencv_core.cvScalar;

import com.googlecode.javacv.cpp.opencv_core.CvScalar;

public class VideoConfiguration {
	public static short NUM_OF_STICKERS = 4;

	public static final short UPPER_VECTOR_INDEX = 0;
	public static final short LOWER_VECTOR_INDEX = 1;
	public static final short LEFT_VECTOR_INDEX = 2;
	public static final short RIGHT_VECTOR_INDEX = 3;


	public static CvScalar UPPER_STICKER_MIN = cvScalar(20, 40, 220, 0); 
	public static CvScalar UPPER_STICKER_MAX = cvScalar(60, 90, 255, 0);

	public static CvScalar LOWER_STICKER_MIN = cvScalar(50, 80, 0, 0);
	public static CvScalar LOWER_STICKER_MAX = cvScalar(110, 180, 90, 0);

	public static CvScalar LEFT_STICKER_MIN = cvScalar(30, 0, 10, 0);
	public static CvScalar LEFT_STICKER_MAX = cvScalar(170, 60, 70, 0);

	public static CvScalar RIGHT_STICKER_MIN = cvScalar(5, 120, 100, 0);
	public static CvScalar RIGHT_STICKER_MAX = cvScalar(50, 200, 255, 0);
	
	public static final String XMLS_URL = "https://dl.dropbox.com/u/8720454/xmls/xmls.zip";

}
