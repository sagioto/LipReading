package edu.lipreading;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import com.googlecode.javacv.cpp.opencv_core.CvScalar;

public class Constants {
    public static final Properties LIP_READING_PROPS = new Properties();
    static {
    	InputStream is = ClassLoader.getSystemResourceAsStream("lr.properties");
        try {
            LIP_READING_PROPS.load(is);
            is.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
    }

    public static final String MPC_MODEL_URL = LIP_READING_PROPS.getProperty("MPC_MODEL");

    public static final String DEFAULT_TRAINING_SET_ZIP = LIP_READING_PROPS.getProperty("DEFAULT_TRAINING_SET_ZIP");

    public static final String DEFAULT_ARFF_FILE = LIP_READING_PROPS.getProperty("DEFAULT_ARFF_FILE");

    public static final String HAAR_CASCADE_MOUTH_FILE = LIP_READING_PROPS.getProperty("HAAR_CASCADE_MOUTH_FILE");

    public static final List<String> VOCABULARY = Utils.readFile(LIP_READING_PROPS.getProperty("DEFAULT_VOCABULARY_FILE"));
    
    public static final int FRAMES_COUNT = Integer.valueOf(LIP_READING_PROPS.getProperty("FRAMES_COUNT"));
    
    public static final int POINT_COUNT = Integer.valueOf(LIP_READING_PROPS.getProperty("POINT_COUNT"));
    
    public static final CvScalar UPPER_STICKER_MIN = Utils.getCvScalar(LIP_READING_PROPS.getProperty("UPPER_STICKER_MIN"));
    public static final CvScalar UPPER_STICKER_MAX = Utils.getCvScalar(LIP_READING_PROPS.getProperty("UPPER_STICKER_MAX"));
    
    public static final CvScalar LOWER_STICKER_MIN = Utils.getCvScalar(LIP_READING_PROPS.getProperty("LOWER_STICKER_MIN"));
    public static final CvScalar LOWER_STICKER_MAX = Utils.getCvScalar(LIP_READING_PROPS.getProperty("LOWER_STICKER_MAX"));
    
    public static final CvScalar LEFT_STICKER_MIN = Utils.getCvScalar(LIP_READING_PROPS.getProperty("LEFT_STICKER_MIN"));
    public static final CvScalar LEFT_STICKER_MAX = Utils.getCvScalar(LIP_READING_PROPS.getProperty("LEFT_STICKER_MAX"));
    
    public static final CvScalar RIGHT_STICKER_MIN = Utils.getCvScalar(LIP_READING_PROPS.getProperty("RIGHT_STICKER_MIN"));
    public static final CvScalar RIGHT_STICKER_MAX = Utils.getCvScalar(LIP_READING_PROPS.getProperty("RIGHT_STICKER_MAX"));

	public static final short UPPER_VECTOR_INDEX = 0;
	public static final short LOWER_VECTOR_INDEX = 1;
	public static final short LEFT_VECTOR_INDEX = 2;
	public static final short RIGHT_VECTOR_INDEX = 3;
	
	//Images:
	public static final String LIP_IMAGE_FILE_PATH = LIP_READING_PROPS.getProperty("LIP_IMAGE_FILE_PATH");
	public static final String FILE_CHOOSER_IMAGE_FILE_PATH = LIP_READING_PROPS.getProperty("FILE_CHOOSER_IMAGE_FILE_PATH");
	public static final String RECORD_IMAGE_FILE_PATH = LIP_READING_PROPS.getProperty("RECORD_IMAGE_FILE_PATH");
	public static final String STOP_IMAGE_FILE_PATH = LIP_READING_PROPS.getProperty("STOP_IMAGE_FILE_PATH");
		
}
