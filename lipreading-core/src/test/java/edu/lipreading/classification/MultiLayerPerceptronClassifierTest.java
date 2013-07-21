package edu.lipreading.classification;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.lipreading.Constants;
import edu.lipreading.LipReading;
import edu.lipreading.Sample;
import edu.lipreading.Utils;
import edu.lipreading.vision.AbstractFeatureExtractor;
import edu.lipreading.vision.NoMoreStickersFeatureExtractor;

public class MultiLayerPerceptronClassifierTest {

	static private MultiLayerPerceptronClassifier mpClassifier;
	
	@Test
	public void testHello() throws Exception{
		testClassifier("https://dl.dropbox.com/u/8720454/test3/Hello37-14.24.32-23.02.2013.MOV", "hello");
	}

	@Test
	public void testYes() throws Exception{
		testClassifier("https://dl.dropbox.com/u/8720454/test3/Yes57-17.46.01-23.02.2013.MOV", "yes");
	}

	@Test
	public void testNo() throws Exception{
		testClassifier("https://dl.dropbox.com/u/8720454/test3/No50-17.46.41-23.02.2013.MOV", "no");
	}

	public void testClassifier(String url, String expected) throws Exception{
		Utils.get(url);
//        AbstractFeatureExtractor fee = new EyesFeatureExtractor();
        AbstractFeatureExtractor fe = new NoMoreStickersFeatureExtractor();
		Sample sample = LipReading.normalize(fe.extract(Utils.getFileNameFromUrl(url)));
		String ans = mpClassifier.test(sample);
		Assert.assertEquals("expected: " + expected + " but got: " + ans, expected, ans);
		new File(Utils.getFileNameFromUrl(url)).delete();
	}

	@BeforeClass
	public static void loadClassifierModel() throws Exception{
        mpClassifier = new MultiLayerPerceptronClassifier(new URL("https://dl.dropbox.com/u/8720454/test3/yesnohello2.model").openStream());
        mpClassifier.setVocabulary(Utils.readFile("vocabularies/primitive.txt"));
	}
	
	@AfterClass
	public static void deleteModelFile() throws UnsupportedEncodingException{
		new File(Utils.getFileNameFromUrl(Constants.CLASSIFIER_MODEL_URL)).delete();
	}

}