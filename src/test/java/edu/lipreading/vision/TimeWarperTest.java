package edu.lipreading.vision;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import junit.framework.Assert;

import org.junit.Test;

import weka.core.xml.XStream;

import com.googlecode.javacv.FrameGrabber.Exception;

import edu.lipreading.Sample;
import edu.lipreading.Utils;
import edu.lipreading.classification.TimeWarper;

public class TimeWarperTest {

	protected static final String FILE_URL = "https://dl.dropbox.com/u/8720454/no-%281%29.MOV";
	protected static final String FILE_URL2 = "https://dl.dropbox.com/u/8720454/no-%281%29SpeedX2.avi";
	protected static final String FILE_URL3 = "https://dl.dropbox.com/u/8720454/no-%2813%29.xml";
	protected static final String XMLS_DIR = "C:\\Users\\Sagi\\Dropbox\\Public\\xmls";
	
	//@Test
	public void DTWTest() throws MalformedURLException, IOException, Exception, InterruptedException, ExecutionException{
		ColoredStickersFeatureExtractor extractor = new ColoredStickersFeatureExtractor();
		TimeWarper tw = new TimeWarper();
		Sample testSample = extractor.extract(FILE_URL);
		Sample trainingSample = extractor.extract(FILE_URL2);
		double ans = tw.dtw(trainingSample, testSample);
		System.out.println("DTW:" + ans);
	}
	
	//@Test
	public void DTWIdentityTest() throws java.lang.Exception{
		Utils.get(FILE_URL3);
		TimeWarper tw = new TimeWarper();
		Sample testSample = (Sample)XStream.read(Utils.getFileNameFromUrl(FILE_URL3));
		double ans = tw.dtw(testSample, testSample);
		System.out.println("DTW:" + ans);
		Assert.assertEquals("the two samples didn't return 0.0", 0.0, ans);
	}
	
	@Test
	public void DTWOnTrainingSetYesTest() throws java.lang.Exception{
		double[] results = DTWOnTrainingSetTest("C:\\Users\\Sagi\\Dropbox\\Public\\yes-(17).xml");
		System.out.println("got yes avg: " + results[1] + " got no avg: " + results[0]);
		Assert.assertTrue("test returned false result", results[0] > results[1]);
	}

	@Test
	public void DTWOnTrainingSetNoTest() throws java.lang.Exception{
		double[] results = DTWOnTrainingSetTest("C:\\Users\\Sagi\\Dropbox\\Public\\no-(17).xml");
		System.out.println("got yes avg: " + results[1] + " got no avg: " + results[0]);
		Assert.assertTrue("test returned false result", results[0] < results[1]);
	}
	
	
	public double[] DTWOnTrainingSetTest(String testFile) throws java.lang.Exception{
		TimeWarper tw = new TimeWarper();
		File samplesDir = new File(XMLS_DIR);
		List<Sample> yesSamples = new Vector<Sample>();
		List<Sample> noSamples = new Vector<Sample>();
		for (String sampleName : samplesDir.list()) {
			File sample = new File(samplesDir.getAbsolutePath()  + "/" + sampleName);
			if(sample.isFile() && sample.getName().contains("xml")){
				Sample read = (Sample) XStream.read(sample);
				if(sampleName.contains("yes"))
					yesSamples.add(read);
				else{
					noSamples.add(read);
				}
			}
		}
		Sample testSample = (Sample) XStream.read(testFile);
		double yes = 0;
		double no = 0;
		for (Sample trainingSample : noSamples) {
			no += tw.dtw(testSample, trainingSample);
		}
		for (Sample trainingSample : yesSamples) {
			yes += tw.dtw(testSample, trainingSample);
		}
		return new double[]{no / noSamples.size(), yes / yesSamples.size()}; 
	}
	

}
