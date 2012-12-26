package edu.lipreading.classification;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.Test;

import weka.core.xml.XStream;
import edu.lipreading.Sample;
import edu.lipreading.Utils;
import edu.lipreading.classification.TimeWarper;
import edu.lipreading.vision.ColoredStickersFeatureExtractor;

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
	public void DTWTest() throws Exception{
		ColoredStickersFeatureExtractor extractor = new ColoredStickersFeatureExtractor();
		TimeWarper tw = new TimeWarper();
		Sample testSample = extractor.extract(FILE_URL);
		Sample trainingSample = extractor.extract(FILE_URL2);
		double ans = tw.dtw(trainingSample, testSample);
		System.out.println("DTW:" + ans);
	}

	@Test
	public void DTWIdentityTest() throws Exception{
		Utils.get(FILE_URL3);
		TimeWarper tw = new TimeWarper();
		Sample testSample = (Sample)XStream.read(Utils.getFileNameFromUrl(FILE_URL3));
		double ans = tw.dtw(testSample, testSample);
		System.out.println("DTW:" + ans);
		Assert.assertEquals("the two samples didn't return 0.0", 0.0, ans);
	}

	@Test
	public void DTWOnTrainingSetYesTest() throws Exception{
		double[] results = DTWOnTrainingSetTest(TEST_YES);
		System.out.println("got yes avg: " + results[YES_INDEX] + " got no avg: " + results[NO_INDEX]);
		Assert.assertTrue("test returned false result", results[YES_INDEX] < results[NO_INDEX]);
	}

	@Test
	public void DTWOnTrainingSetNoTest() throws Exception{
		double[] results = DTWOnTrainingSetTest(TEST_NO);
		System.out.println("got yes avg: " + results[YES_INDEX] + " got no avg: " + results[NO_INDEX]);
		Assert.assertTrue("test returned false result", results[YES_INDEX] > results[NO_INDEX]);
	}

	@Test
	public void massiveProofTest() throws Exception{
		List<Sample> trainingSet = Utils.getTrainingSetFromZip(XMLS_URL);
		TimeWarper tw = new TimeWarper();
		int success = 0, failed = 0;
		for (Sample test : trainingSet) {
			double yes = 0, no = 0;
			int yesCount = 0, noCount = 0;
			for (Sample training : trainingSet) {
				if(!test.equals(training)){
					if(training.getId().contains("yes")){
						yes += tw.dtw(test, training);
						yesCount++;
					}
					else{
						no += tw.dtw(test, training);
						noCount++;
					}
				}
			}

			if(yes / yesCount < no / noCount){
				if(test.getId().contains("yes")){
					success++;
				}
				else{
					System.out.println(test.getId() + " has failed");
					failed++;
				}					
			}
			else{
				if(test.getId().contains("yes")){
					System.out.println(test.getId() + " has failed");
					failed++;
				}
				else{
					success++;
				}
			}
		}
		System.out.println("success:" + success + " failed:" + failed);
		System.out.println("success rate is " + ((100 * success) / (success + failed)) + "%");
		Assert.assertTrue(success > failed);
	}

	public double[] DTWOnTrainingSetTest(String testFile) throws Exception{
		Utils.get(testFile);
		TimeWarper tw = new TimeWarper();
		List<Sample> trainingSet = Utils.getTrainingSetFromZip(XMLS_URL);
		Sample testSample = (Sample) XStream.read(Utils.getFileNameFromUrl(testFile));
		double yes = 0, no = 0;
		int yesCount = 0, noCount = 0;
		for (Sample trainingSample : trainingSet) {
			if(!trainingSample.equals(testSample)){
				if(trainingSample.getId().contains("yes")){
					yes += tw.dtw(testSample, trainingSample);
					yesCount++;
				}
				else{
					no += tw.dtw(testSample, trainingSample);
					noCount++;
				}
			}
		}
		return new double[]{yes / yesCount, no / noCount};
	}


	@AfterClass
	public static void deleteFile() throws UnsupportedEncodingException{
		new File(Utils.getFileNameFromUrl(FILE_URL)).delete();
		new File(Utils.getFileNameFromUrl(FILE_URL2)).delete();
		new File(Utils.getFileNameFromUrl(FILE_URL3)).delete();
		new File(Utils.getFileNameFromUrl(XMLS_URL)).delete();
		new File(Utils.getFileNameFromUrl(TEST_NO)).delete();
		new File(Utils.getFileNameFromUrl(TEST_YES)).delete();
	}

}
