package edu.lipsreading.vision;

import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.Test;

public class LipsReadingTest {

	@Test
	public void dumyTest() throws MalformedURLException, IOException{
		LipReading.get("https://dl.dropbox.com/u/8720454/Hello%20%281%29.3gp", "hello1.mp4");
		System.out.println("just pass");
	}
}
