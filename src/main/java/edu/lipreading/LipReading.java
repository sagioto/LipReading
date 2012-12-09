package edu.lipreading;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import weka.core.xml.XStream;
import edu.lipreading.classification.Classifier;
import edu.lipreading.classification.TimeWarperClassifier;
import edu.lipreading.normalization.CenterNormalizer;
import edu.lipreading.normalization.Normalizer;
import edu.lipreading.vision.AbstractFeatureExtractor;
import edu.lipreading.vision.ColoredStickersFeatureExtractor;




public class LipReading {

	private static final String DEFAULT_TRAINING_SET_ZIP_URL = "https://dl.dropbox.com/u/8720454/xmls/xmls.zip";

	public static void main(String...args) throws Exception{
		List<String> argsAsList = Arrays.asList(args);
		if(args.length == 0 || argsAsList.contains("-help")){
			System.out.println("usage:");
			System.out.println("\t-help : prints this message");
			System.out.println("\t-extract <video file url, name or 0 for webcam> : extracts Sample object from the file into an xml");
			System.out.println("\t-output : when used with -extract records an output video");
			System.out.println("\t-dataset <path of folder of video files> - go through all the video files and" +
					"\n\t generate Sample xmls for each of them");
			System.out.println("\t-test <video file url, name, 0 for webcam or xml> : uses the input file as test against a default data set");
			System.out.println("\t-test <video file url, name, 0 for webcam or xml>  <zip file url> : uses the input file as test against " +
					"\n\t the data set in the given zip file url");
			System.out.println("\t-csv <input zip file, output file> : converts zip data set into csv one");
			System.exit(0);
		}

		Normalizer normalizer = new CenterNormalizer();
		AbstractFeatureExtractor fe = new ColoredStickersFeatureExtractor();

		if(argsAsList.contains("-extract")){
			String sampleName = args[argsAsList.lastIndexOf("-extract") + 1];
			fe.setOutput(argsAsList.contains("-output"));
			XStream.write(sampleName.split("\\.")[0] + ".xml", normalizer.normalize(fe.extract(sampleName)));
		}
		else if(argsAsList.contains("-database")){
			dataset(normalizer, fe, args[argsAsList.lastIndexOf("-database") + 1]);
		}
		else if(argsAsList.contains("-test") && argsAsList.size() >= argsAsList.lastIndexOf("-test") + 2){
			test(normalizer, fe, args[argsAsList.lastIndexOf("-test") + 1], args[argsAsList.lastIndexOf("-test") + 2]);
		}
		else if(argsAsList.contains("-test")){
			test(normalizer, fe, args[argsAsList.lastIndexOf("-test") + 1], DEFAULT_TRAINING_SET_ZIP_URL);
		}
		else if(argsAsList.contains("-csv")){
			Utils.dataSetToCSV(args[argsAsList.lastIndexOf("-csv") + 1], args[argsAsList.lastIndexOf("-csv") + 2]);
		}

		System.exit(0);
	}

	

	private static void test(Normalizer normalizer,
			AbstractFeatureExtractor fe, String testFile, String trainigSetZipFile) throws Exception {
		Classifier classifier = new TimeWarperClassifier(); 

		System.out.println("got the word: " +
				classifier.classify(Utils.getTrainingSetFromZip(trainigSetZipFile),
						normalizer.normalize(
								fe.extract(testFile))));
	}

	private static void dataset(Normalizer normalizer,
			AbstractFeatureExtractor fe, String folderPath) throws Exception {
		File samplesDir = new File(folderPath);
		for (String sampleName : samplesDir.list()) {
			File sample = new File(samplesDir.getAbsolutePath()  + "/" + sampleName);
			if(sample.isFile() && sample.getName().contains("MOV"))
				XStream.write(sampleName.split("\\.")[0] + ".xml", normalizer.normalize(fe.extract(sample.getAbsolutePath())));
		}
	}


}
