package edu.lipsreading.vision;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import junit.framework.Assert;

import org.junit.Test;

public class LipsReadingTest {

	@Test
	public void dumyTest() throws MalformedURLException, IOException{
		String filename = "hello1.mp4";
		LipReading.get("https://dl.dropbox.com/u/8720454/Hello%20%281%29.3gp", filename);
		Assert.assertTrue("file was not downloaded", new File(filename).exists());
	}
}
