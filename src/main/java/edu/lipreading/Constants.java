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

    public static final String DEFAULT_TRAINING_SET_ZIP_URL = LIP_READING_PROPS.getProperty("DEFAULT_TRAINING_SET_ZIP_URL");

    public static final String DEFAULT_ARFF_FILE = LIP_READING_PROPS.getProperty("DEFAULT_ARFF_FILE");

    public static final List<String> VOCABULARY = Utils.readFile(LIP_READING_PROPS.getProperty("DEFAULT_VOCABULARY_FILE"));
    
    
    
    public static final CvScalar UPPER_STICKER_MIN = Utils.getCvScalar(LIP_READING_PROPS.getProperty("UPPER_STICKER_MIN"));
    public static final CvScalar UPPER_STICKER_MAX = Utils.getCvScalar(LIP_READING_PROPS.getProperty("UPPER_STICKER_MAX"));
    public static final CvScalar LOWER_STICKER_MIN = Utils.getCvScalar(LIP_READING_PROPS.getProperty("LOWER_STICKER_MIN"));
    public static final CvScalar LOWER_STICKER_MAX = Utils.getCvScalar(LIP_READING_PROPS.getProperty("LOWER_STICKER_MAX"));
    public static final CvScalar LEFT_STICKER_MIN = Utils.getCvScalar(LIP_READING_PROPS.getProperty("LEFT_STICKER_MIN"));
    public static final CvScalar LEFT_STICKER_MAX = Utils.getCvScalar(LIP_READING_PROPS.getProperty("LEFT_STICKER_MAX"));
    public static final CvScalar RIGHT_STICKER_MIN = Utils.getCvScalar(LIP_READING_PROPS.getProperty("RIGHT_STICKER_MIN"));
    public static final CvScalar RIGHT_STICKER_MAX = Utils.getCvScalar(LIP_READING_PROPS.getProperty("RIGHT_STICKER_MAX"));
}
