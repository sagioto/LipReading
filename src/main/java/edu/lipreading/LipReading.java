package edu.lipreading;

import java.io.File;
import java.util.Arrays;

import weka.core.xml.XStream;
import edu.lipreading.classification.Classifier;
import edu.lipreading.classification.NullClassifier;
import edu.lipreading.normalization.EmptyNomelizer;
import edu.lipreading.normalization.Normalizer;
import edu.lipreading.vision.AbstractFeatureExtractor;
import edu.lipreading.vision.ColoredStickersFeatureExtractor;




public class LipReading {

	public static void main(String...args) throws Exception{
		if(args.length == 0 || Arrays.asList(args).contains("-help")){
			System.out.println("usage:");
			System.out.println("\t-help : prints this message");
			System.out.println("\t-extract <video file url, name or 0 for webcam> : extracts Sample object from the file into an xml");
			System.out.println("\t-output : when used with -extract records an output video");
			System.out.println("\t-dataset <path of folder of video files> - go through all the video files and" +
					"\n\t generate Sample xmls for each of them");
			System.out.println("\t-test <video file url, name, 0 for webcam or xml> : uses the input file as test against a default data set");
			System.out.println("\t-test <video file url, name, 0 for webcam or xml>  <zip file url> : uses the input file as test against " +
					"\n\t the data set in the given zip file url");
			System.exit(0);
		}

		File samplesDir = new File(args[0]);
		Normalizer normalizer = new EmptyNomelizer();
		AbstractFeatureExtractor fe = new ColoredStickersFeatureExtractor();

		for (String sampleName : samplesDir.list()) {
			File sample = new File(samplesDir.getAbsolutePath()  + "/" + sampleName);
			if(sample.isFile() && sample.getName().contains("MOV"))
				XStream.write(sampleName.split("\\.")[0] + ".xml", normalizer.normelize(fe.extract(sample.getAbsolutePath())));
		}
		Classifier classifier = new NullClassifier(); 

		System.out.println("got the word: " +
				classifier.classify(null,
						normalizer.normelize(
								fe.extract(args[0]))));
		System.exit(0);
	}


}
