package edu.lipreading.vision;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.googlecode.javacv.FrameGrabber.Exception;

import edu.lipreading.Utils;


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
	public void readFromFileTest() throws MalformedURLException, IOException, Exception, InterruptedException, ExecutionException{
		Utils.get(FILE_URL);
		File file = new File(Utils.getFileNameFromUrl(FILE_URL));
		Assert.assertTrue("file was not downloaded", file.exists());
		new NullFeatureExtrcator().extract(file.getName());
	}

	@Test
	public void readFromUrlTest() throws MalformedURLException, IOException, Exception, InterruptedException, ExecutionException{
		new ColoredStickersFeatureExtractor().extract(FILE_URL);
	}
	


	@AfterClass
	public static void teardown() throws UnsupportedEncodingException {
	    cleanFile();
	}
	
	
}
