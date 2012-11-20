package edu.lipreading.vision;

import java.io.IOException;
import java.net.MalformedURLException;

import junit.framework.Assert;

import org.junit.Test;

import weka.core.xml.XStream;

import com.googlecode.javacv.FrameGrabber.Exception;
import com.thoughtworks.xstream.XStreamer;

import edu.lipreading.Sample;
import edu.lipreading.Utils;
import edu.lipreading.classification.TimeWarper;

public class TimeWarperTest {

	protected static final String FILE_URL = "https://dl.dropbox.com/u/8720454/no-%281%29.MOV";
	protected static final String FILE_URL2 = " https://dl.dropbox.com/u/8720454/no-%281%29SpeedX2.avi";
	protected static final String FOLDER_URL = "https://dl.dropbox.com/u/8720454/xmls";
	
	//@Test
	public void testDTW() throws MalformedURLException, IOException, Exception, InterruptedException{
		ColoredStickersFeatureExtractor extractor = new ColoredStickersFeatureExtractor();
		TimeWarper tw = new TimeWarper();
		Sample testSample = extractor.extract(FILE_URL);
		Sample trainingSample = extractor.extract(FILE_URL2);
		double ans = tw.dtw(trainingSample, testSample);
		System.out.println("DTW:" + ans);
	}
	
	//@Test
	public void testDTWIdentity() throws java.lang.Exception{
		Utils.get(FILE_URL);
		TimeWarper tw = new TimeWarper();
		Sample testSample = (Sample)XStream.read(Utils.getFileNameFromUrl(FILE_URL));
		
		double ans = tw.dtw(testSample, testSample);
		System.out.println("DTW:" + ans);
		Assert.assertEquals("the two samples didn't return 0", 0, ans);
	}
	
	@Test
	public void testDTWOnTrainingSet() throws java.lang.Exception{
		Utils.get(FOLDER_URL);
		TimeWarper tw = new TimeWarper();
		Sample testSample = (Sample)XStream.read(Utils.getFileNameFromUrl(FILE_URL));
		
		double ans = tw.dtw(testSample, testSample);
		System.out.println("DTW:" + ans);
		Assert.assertEquals("the two samples didn't return 0", 0, ans);
	}
	

}
