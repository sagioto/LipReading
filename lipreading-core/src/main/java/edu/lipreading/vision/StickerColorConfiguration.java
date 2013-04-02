package edu.lipreading.vision;

import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import edu.lipreading.Constants;
import edu.lipreading.Utils;

public class StickerColorConfiguration {

    public static CvScalar UPPER_STICKER_MIN = Utils.getCvScalar(Constants.LIP_READING_PROPS.getProperty("UPPER_STICKER_MIN"));
    public static CvScalar UPPER_STICKER_MAX = Utils.getCvScalar(Constants.LIP_READING_PROPS.getProperty("UPPER_STICKER_MAX"));

    public static CvScalar LOWER_STICKER_MIN = Utils.getCvScalar(Constants.LIP_READING_PROPS.getProperty("LOWER_STICKER_MIN"));
    public static CvScalar LOWER_STICKER_MAX = Utils.getCvScalar(Constants.LIP_READING_PROPS.getProperty("LOWER_STICKER_MAX"));

    public static CvScalar LEFT_STICKER_MIN = Utils.getCvScalar(Constants.LIP_READING_PROPS.getProperty("LEFT_STICKER_MIN"));
    public static CvScalar LEFT_STICKER_MAX = Utils.getCvScalar(Constants.LIP_READING_PROPS.getProperty("LEFT_STICKER_MAX"));

    public static CvScalar RIGHT_STICKER_MIN = Utils.getCvScalar(Constants.LIP_READING_PROPS.getProperty("RIGHT_STICKER_MIN"));
    public static CvScalar RIGHT_STICKER_MAX = Utils.getCvScalar(Constants.LIP_READING_PROPS.getProperty("RIGHT_STICKER_MAX"));
}