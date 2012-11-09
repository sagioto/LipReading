package edu.lipreading;

import edu.lipreading.classify.NullClassifier;
import edu.lipreading.vision.NullFeatureExtrcator;




public class LipReading {

	public static void main(String...args) throws Exception{
		System.out.println("got the word: " +
				new NullClassifier().classify(
						new NullFeatureExtrcator().extract(args[0])));
		System.exit(0);
	}

	
}
