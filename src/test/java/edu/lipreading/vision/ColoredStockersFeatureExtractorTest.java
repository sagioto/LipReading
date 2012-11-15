package edu.lipreading.vision;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import junit.framework.Assert;

import org.junit.Test;

import com.googlecode.javacv.FrameGrabber.Exception;

import edu.lipreading.Utils;

public class ColoredStockersFeatureExtractorTest extends FeatureExtractorTest{

    @Override
    @Test
    public void readFromFileTest() throws MalformedURLException, IOException, Exception, InterruptedException{
        Utils.get(FILE_URL);
        File file = new File(Utils.getFileNameFromUrl(FILE_URL));
        Assert.assertTrue("file was not downloaded", file.exists());
        new ColoredStickersFeatureExtractor().extract(file.getName());
    }
    
    @Override
    public void downloadTest() throws MalformedURLException, IOException{}
    
    @Override
    public void readFromUrlTest() throws MalformedURLException, IOException, Exception, InterruptedException{}
}
