package edu.lipreading;

import edu.lipreading.classification.Classifier;
import edu.lipreading.classification.NullClassifier;
import edu.lipreading.normalization.EmptyNomelizer;
import edu.lipreading.normalization.Normelizer;
import edu.lipreading.vision.AbstractFeatureExtractor;
import edu.lipreading.vision.ColoredStickersFeatureExtractor;




public class LipReading {

	public static void main(String...args) throws Exception{
		AbstractFeatureExtractor fe = new ColoredStickersFeatureExtractor();
		Normelizer normelizer = new EmptyNomelizer();
		Classifier classifier = new NullClassifier(); 
		
		System.out.println("got the word: " +
				classifier.classify(null,
						normelizer.normelize(
								fe.extract(args[0]))));
		System.exit(0);
	}

	
}
