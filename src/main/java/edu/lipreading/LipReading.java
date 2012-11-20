package edu.lipreading;

import java.io.File;

import weka.core.xml.XStream;
import edu.lipreading.classification.Classifier;
import edu.lipreading.classification.NullClassifier;
import edu.lipreading.normalization.EmptyNomelizer;
import edu.lipreading.normalization.Normelizer;
import edu.lipreading.vision.AbstractFeatureExtractor;
import edu.lipreading.vision.ColoredStickersFeatureExtractor;




public class LipReading {

	public static void main(String...args) throws Exception{
		File samplesDir = new File(args[0]);
		Normelizer normelizer = new EmptyNomelizer();
		AbstractFeatureExtractor fe = new ColoredStickersFeatureExtractor();

		
		for (String sampleName : samplesDir.list()) {
			File sample = new File(samplesDir.getAbsolutePath()  + "/" + sampleName);
			if(sample.isFile() && sample.getName().contains("MOV"))
				XStream.write(sampleName.split("\\.")[0] + ".xml", normelizer.normelize(fe.extract(sample.getAbsolutePath())));
		}
		Classifier classifier = new NullClassifier(); 

		System.out.println("got the word: " +
				classifier.classify(null,
						normelizer.normelize(
								fe.extract(args[0]))));
		System.exit(0);
	}


}
