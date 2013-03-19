package edu.lipreading.classification;

import edu.lipreading.Sample;
import edu.lipreading.Utils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import weka.core.xml.XStream;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

public class TimeWarperTest {

	protected static final String FILE_URL = "https://dl.dropbox.com/u/8720454/no-%281%29.MOV";
	protected static final String FILE_URL2 = "https://dl.dropbox.com/u/8720454/no-%281%29SpeedX2.avi";
	protected static final String FILE_URL3 = "https://dl.dropbox.com/u/8720454/xmls/no-%2813%29.xml";
	protected static final String XMLS_URL = "https://dl.dropbox.com/u/8720454/xmls/xmls.zip";
	protected static final String DATA_SET2_URL = "https://dl.dropbox.com/u/8720454/set2/dataset.zip";
	protected static final String TEST_YES = "https://dl.dropbox.com/u/8720454/xmls/yes-%2817%29.xml";
	protected static final String TEST_NO = "https://dl.dropbox.com/u/8720454/xmls/no-%2817%29.xml";
	protected static final int YES_INDEX = 0;
	protected static final int NO_INDEX = 1;


	@Test
	public void DTWIdentityTest() throws Exception{
		Utils.get(FILE_URL3);
		TimeWarper tw = new TimeWarper();
		Sample testSample = (Sample)XStream.read(Utils.getFileNameFromUrl(FILE_URL3));
		double ans = tw.dtw(testSample, testSample);
		System.out.println("DTW:" + ans);
		Assert.assertEquals("the two samples didn't return 0.0", 0.0, ans, 0);
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
	public void massiveProofTest1() throws Exception{
		massiveProofTest(Arrays.asList("yes", "no"), XMLS_URL);
	}

	@Test
	public void massiveProofTest2() throws Exception{
		massiveProofTest(Arrays.asList("yes", "no", "hello"), DATA_SET2_URL);
	}

	public void massiveProofTest(List<String> vocabulary, String dataSetUrl) throws Exception{
		List<Sample> trainingSet = Utils.getTrainingSetFromZip(dataSetUrl);
		double [][] results = new double[trainingSet.size()][vocabulary.size()];
		TimeWarper tw = new TimeWarper();
		int success = 0, failed = 0;
		for (int i = 0; i < trainingSet.size(); i++) {
			int [] counts = new int[vocabulary.size()];
			for (Sample training : trainingSet) {
				if(!trainingSet.get(i).equals(training)){
					for (int j = 0; j < vocabulary.size(); j++) {
						if(training.getId().contains(vocabulary.get(j))){
							results[i][j] += tw.dtw(trainingSet.get(i), training);
							counts[j]++;
						}
					}
				}
			}
			for (int j = 0; j < vocabulary.size(); j++) {
				results[i][j] /= counts[j];
			}
			int minIndex = Utils.getMinIndex(results[i]);
			if(trainingSet.get(i).getId().contains(vocabulary.get(minIndex))){
				success++;
			}
			else{
				failed++;
				System.out.println(trainingSet.get(i).getId() + " has failed");
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
		new File(Utils.getFileNameFromUrl(DATA_SET2_URL)).delete();
		new File(Utils.getFileNameFromUrl(TEST_NO)).delete();
		new File(Utils.getFileNameFromUrl(TEST_YES)).delete();
	}

}
