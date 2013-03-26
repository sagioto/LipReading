package edu.lipreading.gui;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Constants {
    public static final Properties LIP_READING_PROPS = new Properties();
    static {
    	InputStream is = ClassLoader.getSystemResourceAsStream("lr-gui.properties");
        try {
            LIP_READING_PROPS.load(is);
            is.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
    }

	public static final String LIP_IMAGE_FILE_PATH = LIP_READING_PROPS.getProperty("LIP_IMAGE_FILE_PATH");
	public static final String FILE_CHOOSER_IMAGE_FILE_PATH = LIP_READING_PROPS.getProperty("FILE_CHOOSER_IMAGE_FILE_PATH");
	public static final String RECORD_IMAGE_FILE_PATH = LIP_READING_PROPS.getProperty("RECORD_IMAGE_FILE_PATH");
	public static final String STOP_IMAGE_FILE_PATH = LIP_READING_PROPS.getProperty("STOP_IMAGE_FILE_PATH");
	public static final String LIP_READING_TITLE = LIP_READING_PROPS.getProperty("LIP_READING_TITLE");
	public static final String LR_ICON = LIP_READING_PROPS.getProperty("LR_ICON");
	
	
	
}
