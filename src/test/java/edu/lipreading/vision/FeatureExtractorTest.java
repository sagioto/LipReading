package edu.lipreading.vision;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.googlecode.javacv.FrameGrabber.Exception;

import edu.lipreading.Sample;
import edu.lipreading.Utils;
import edu.lipreading.classification.TimeWarper;

public class FeatureExtractorTest {

	protected static final String FILE_URL = "https://dl.dropbox.com/u/8720454/no-%281%29.MOV";

	@Before
	public void setUp() throws UnsupportedEncodingException {
		cleanFile();
	}
	
    private static void cleanFile() throws UnsupportedEncodingException {
        File file = new File(Utils.getFileNameFromUrl(FILE_URL));
		if(file.exists())
			Assert.assertTrue("file was not deleted", file.delete());
    }

	//@Test
	public void downloadTest() throws MalformedURLException, IOException{
		Utils.get(FILE_URL);
		Assert.assertTrue("file was not downloaded", new File(Utils.getFileNameFromUrl(FILE_URL)).exists());
	}

	//@Test
	public void readFromFileTest() throws MalformedURLException, IOException, Exception, InterruptedException{
		Utils.get(FILE_URL);
		File file = new File(Utils.getFileNameFromUrl(FILE_URL));
		Assert.assertTrue("file was not downloaded", file.exists());
		new NullFeatureExtrcator().extract(file.getName());
	}

	@Test
	public void readFromUrlTest() throws MalformedURLException, IOException, Exception, InterruptedException{
		new ColoredStickersFeatureExtractor().extract(FILE_URL);
	}
	
	@Test
	public void getDTW() throws MalformedURLException, IOException, Exception, InterruptedException{
		ColoredStickersFeatureExtractor extractor = new ColoredStickersFeatureExtractor();
		TimeWarper tw = new TimeWarper();
		Sample testSample = extractor.extract(FILE_URL);
		Sample trainingSample = extractor.extract(FILE_URL);
		tw.dtw(testSample, trainingSample);
		
	}

	@AfterClass
	public static void teardown() throws UnsupportedEncodingException {
	    cleanFile();
	}
	
	
}
