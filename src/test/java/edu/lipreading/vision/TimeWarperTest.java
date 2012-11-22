package edu.lipreading.vision;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.Test;

import weka.core.xml.XStream;

import com.googlecode.javacv.FrameGrabber.Exception;

import edu.lipreading.Sample;
import edu.lipreading.Utils;
import edu.lipreading.classification.TimeWarper;

public class TimeWarperTest {

	protected static final String FILE_URL = "https://dl.dropbox.com/u/8720454/no-%281%29.MOV";
	protected static final String FILE_URL2 = "https://dl.dropbox.com/u/8720454/no-%281%29SpeedX2.avi";
	protected static final String FILE_URL3 = "https://dl.dropbox.com/u/8720454/xmls/no-%2813%29.xml";
	protected static final String XMLS_URL = "https://dl.dropbox.com/u/8720454/xmls/xmls.zip";
	protected static final String TEST_YES = "https://dl.dropbox.com/u/8720454/xmls/yes-%2817%29.xml";
	protected static final String TEST_NO = "https://dl.dropbox.com/u/8720454/xmls/no-%2817%29.xml";
	protected static final int YES_INDEX = 0;
	protected static final int NO_INDEX = 1;

	@Test
	public void DTWTest() throws MalformedURLException, IOException, Exception, InterruptedException, ExecutionException{
		ColoredStickersFeatureExtractor extractor = new ColoredStickersFeatureExtractor();
		TimeWarper tw = new TimeWarper();
		Sample testSample = extractor.extract(FILE_URL);
		Sample trainingSample = extractor.extract(FILE_URL2);
		double ans = tw.dtw(trainingSample, testSample);
		System.out.println("DTW:" + ans);
	}

	@Test
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
		double[] results = DTWOnTrainingSetTest(TEST_YES);
		System.out.println("got yes avg: " + results[YES_INDEX] + " got no avg: " + results[NO_INDEX]);
		Assert.assertTrue("test returned false result", results[YES_INDEX] < results[NO_INDEX]);
	}

	@Test
	public void DTWOnTrainingSetNoTest() throws java.lang.Exception{
		double[] results = DTWOnTrainingSetTest(TEST_NO);
		System.out.println("got yes avg: " + results[YES_INDEX] + " got no avg: " + results[NO_INDEX]);
		Assert.assertTrue("test returned false result", results[YES_INDEX] > results[NO_INDEX]);
	}

	@Test
	public void massiveProofTest() throws java.lang.Exception{
		List<List<Sample>> trainingSet = getTrainingSetFromZip(XMLS_URL);
		TimeWarper tw = new TimeWarper();
		int index = 0, success = 0, failed = 0;
		for (List<Sample> list : trainingSet) {

			for (Sample sample : list) {
				double yes = 0, no = 0;
				for (Sample trainingSample : trainingSet.get(YES_INDEX)) {
					if(!trainingSample.equals(sample))
						yes += tw.dtw(sample, trainingSample);
				}
				for (Sample trainingSample : trainingSet.get(NO_INDEX)) {
					if(!trainingSample.equals(sample))
						no += tw.dtw(sample, trainingSample);
				}
				switch(index){
				case YES_INDEX:
					if(yes / trainingSet.get(YES_INDEX).size() < no / trainingSet.get(NO_INDEX).size())
						success++;
					else if(no / trainingSet.get(NO_INDEX).size() < yes / trainingSet.get(YES_INDEX).size())
						failed++;
					break;
				case NO_INDEX:
					if(yes / trainingSet.get(YES_INDEX).size() > no / trainingSet.get(NO_INDEX).size())
						success++;
					else if(no / trainingSet.get(NO_INDEX).size() > yes / trainingSet.get(YES_INDEX).size())
						failed++;
					break;
				}
			}
			index++;
		}
		System.out.println("success:" + success + " failed:" + failed);
		System.out.println("success rate is " + ((100 * success) / (success + failed)) + "%");
		Assert.assertTrue(success > failed);
	}


	public double[] DTWOnTrainingSetTest(String testFile) throws java.lang.Exception{
		Utils.get(testFile);
		TimeWarper tw = new TimeWarper();
		List<List<Sample>> trainingSet = getTrainingSetFromZip(XMLS_URL);
		Sample testSample = (Sample) XStream.read(Utils.getFileNameFromUrl(testFile));
		double yes = 0, no = 0;
		for (Sample trainingSample : trainingSet.get(YES_INDEX)) {
			if(!trainingSample.equals(testSample))
				yes += tw.dtw(testSample, trainingSample);
		}
		for (Sample trainingSample : trainingSet.get(NO_INDEX)) {
			if(!trainingSample.equals(testSample))
				no += tw.dtw(testSample, trainingSample);
		}
		return new double[]{yes / trainingSet.get(YES_INDEX).size(), no / trainingSet.get(NO_INDEX).size()};
	}

	public static List<List<Sample>> getTrainingSetFromZip(String zipUrl) throws MalformedURLException,
	IOException, UnsupportedEncodingException, java.lang.Exception {
		Utils.get(zipUrl);
		ZipFile samplesZip = new ZipFile(Utils.getFileNameFromUrl(zipUrl));
		List<List<Sample>> trainingSet = new Vector<List<Sample>>();
		trainingSet.add(new Vector<Sample>());
		trainingSet.add(new Vector<Sample>());
		Enumeration<? extends ZipEntry> entries = samplesZip.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();

			Sample read = (Sample) XStream.read(samplesZip.getInputStream(entry));
			if(entry.getName().contains("yes"))
				trainingSet.get(YES_INDEX).add(read);
			else{
				trainingSet.get(NO_INDEX).add(read);
			}
		}
		samplesZip.close();
		return trainingSet;
	}


	@AfterClass
	public static void deleteFile() throws UnsupportedEncodingException{
		Assert.assertTrue(new File(Utils.getFileNameFromUrl(FILE_URL)).delete());
		Assert.assertTrue(new File(Utils.getFileNameFromUrl(FILE_URL2)).delete());
		Assert.assertTrue(new File(Utils.getFileNameFromUrl(FILE_URL3)).delete());
		Assert.assertTrue(new File(Utils.getFileNameFromUrl(XMLS_URL)).delete());
		Assert.assertTrue(new File(Utils.getFileNameFromUrl(TEST_NO)).delete());
		Assert.assertTrue(new File(Utils.getFileNameFromUrl(TEST_YES)).delete());
	}

}
