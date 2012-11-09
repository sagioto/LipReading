package edu.lipreading.vision;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.googlecode.javacv.FrameGrabber.Exception;

import edu.lipreading.Utils;
import edu.lipreading.vision.NullFeatureExtrcator;

public class FeatureExtractorTest {

	private static final String FILE_URL = "https://dl.dropbox.com/u/8720454/Hello%20%281%29.3gp";
	private static final String FILE_NAME = "hello1.3gp";

	@Before
	public void setUp() {
		File file = new File(FILE_NAME);
		if(file.exists())
			Assert.assertTrue("file was not deleted", file.delete());
	}

	@Test
	public void downloadTest() throws MalformedURLException, IOException{
		Utils.get(FILE_URL, FILE_NAME);
		Assert.assertTrue("file was not downloaded", new File(FILE_NAME).exists());
	}

	@Test
	public void readFromFileTest() throws MalformedURLException, IOException, Exception{
		Utils.get(FILE_URL, FILE_NAME);
		File file = new File(FILE_NAME);
		Assert.assertTrue("file was not downloaded", file.exists());
		new NullFeatureExtrcator().extract(file.getName());
	}

	@Test
	public void readFromUrlTest() throws MalformedURLException, IOException, Exception{
		new NullFeatureExtrcator().extract(FILE_URL);
	}

	
	
}
