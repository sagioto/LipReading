package edu.lipreading.classification;

import java.io.File;
import java.io.UnsupportedEncodingException;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.lipreading.Constants;
import edu.lipreading.LipReading;
import edu.lipreading.Sample;
import edu.lipreading.Utils;
import edu.lipreading.normalization.CenterNormalizer;
import edu.lipreading.normalization.Normalizer;
import edu.lipreading.normalization.SimpleTimeNormalizer;
import edu.lipreading.vision.AbstractFeatureExtractor;
import edu.lipreading.vision.ColoredStickersFeatureExtractor;

public class WekaModelClassifierTest {
	private Classifier mpClassifier;
	
	@Test
	public void testHello() throws Exception{
		testClassifier("https://dl.dropbox.com/u/8720454/set2/hello/hello-18.MOV", "hello");
	}

	@Test
	public void testYes() throws Exception{
		testClassifier("https://dl.dropbox.com/u/8720454/set2/yes/yes-23.MOV", "yes");
	}

	@Test
	public void testNo() throws Exception{
		testClassifier("https://dl.dropbox.com/u/8720454/set2/no/no-9.MOV", "no");
	}
	
	
	
	public void testClassifier(String url, String expected) throws Exception{
		Utils.get(url);
		Normalizer cn = new CenterNormalizer();
		Normalizer stn = new SimpleTimeNormalizer();
		AbstractFeatureExtractor fe = new ColoredStickersFeatureExtractor();
		Sample sample = LipReading.normelize(fe.extract(Utils.getFileNameFromUrl(url)), cn, stn);
		String ans = mpClassifier.test(sample);
		Assert.assertEquals("expected: " + expected + " but got: " + ans, expected, ans);
		new File(Utils.getFileNameFromUrl(url)).delete();
	}
	
	@BeforeClass
	public void loadClassifierModel() throws Exception{
		mpClassifier = new WekaModelClassifier(Constants.MPC_MODEL_URL);
	}
	
	@AfterClass
	public static void deleteModelFile() throws UnsupportedEncodingException{
		new File(Utils.getFileNameFromUrl(Constants.MPC_MODEL_URL)).delete();
	}
}
