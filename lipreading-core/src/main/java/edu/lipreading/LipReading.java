package edu.lipreading;

import edu.lipreading.classification.Classifier;
import edu.lipreading.classification.MultiLayerPerceptronClassifier;
import edu.lipreading.classification.SVMClassifier;
import edu.lipreading.classification.WekaClassifier;
import edu.lipreading.normalization.CenterNormalizer;
import edu.lipreading.normalization.LinearStretchTimeNormalizer;
import edu.lipreading.normalization.Normalizer;
import edu.lipreading.normalization.SkippedFramesNormalizer;
import edu.lipreading.vision.AbstractFeatureExtractor;
import edu.lipreading.vision.ColoredStickersFeatureExtractor;
import weka.core.xml.XStream;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;


public class LipReading {

    public static void main(String... args) throws Exception {
        List<String> argsAsList = Arrays.asList(args);
        if (args.length == 0 || argsAsList.contains("-help")) {
            System.out.println("usage:");
            System.out.println("-help : prints this message");
            System.out.println("-arff <input-training-set.zip> <output.arff> : Generates a Weka ARFF file from the given training set zip");
            System.out.println("-train <input-training-set.zip> <output.model> : Trains a classifier model and saves it to file");
            System.out.println("-verifyModel <classifier.model> <training-set.zip> : Loads a classifier from file and tests it against the given zip file");
            System.out.println("-extract <video file url, name or 0 for webcam> : extracts Sample object from the file into an xml");
            System.out.println("-output : when used with -extract records an output video");
            System.out.println("-dataset <path of folder of video files> : go through all the video files and" +
                    "\n generate Sample xmls for each of them");
            System.out.println("-test <video file url, name, 0 for webcam or xml> : uses the input file as test against a default data set");
            System.out.println("-test <video file url, name, 0 for webcam or xml>  <zip file url> : uses the input file as test against " +
                    "\n the data set in the given zip file url");
            System.out.println("-csv <input zip file, output file> : converts zip data set into csv one");
            System.exit(0);
        }

        Normalizer cn = new CenterNormalizer();
        AbstractFeatureExtractor fe = new ColoredStickersFeatureExtractor();

        if (argsAsList.contains("-extract")) {
            String sampleName = args[argsAsList.lastIndexOf("-extract") + 1];
            fe.setOutput(argsAsList.contains("-output"));
            XStream.write(sampleName.split("\\.")[0] + ".xml", cn.normalize(fe.extract(sampleName)));
        } else if (argsAsList.contains("-train")) {
            List<Sample> trainingSet = Utils.getTrainingSetFromZip(args[argsAsList.lastIndexOf("-train") + 1]);

            WekaClassifier svmc = new SVMClassifier();
            String tmpArff = args[argsAsList.lastIndexOf("-train") + 2] + "tmp.arff";
            Utils.dataSetToARFF(trainingSet, tmpArff);
            svmc.trainFromFile(tmpArff);
            svmc.saveToFile(args[argsAsList.lastIndexOf("-train") + 2]);
            testModel(trainingSet, svmc);
            new File(tmpArff).deleteOnExit();
        } else if (argsAsList.contains("-dataset")) {
            dataset(fe, args[argsAsList.lastIndexOf("-dataset") + 1]);
        } else if (argsAsList.contains("-test") && argsAsList.size() > argsAsList.lastIndexOf("-test") + 2) {
            test(fe, args[argsAsList.lastIndexOf("-test") + 1], args[argsAsList.lastIndexOf("-test") + 2]);
        } else if (argsAsList.contains("-test")) {
            test(fe, args[argsAsList.lastIndexOf("-test") + 1], Constants.DEFAULT_TRAINING_SET_ZIP);
        } else if (argsAsList.contains("-csv")) {
            List<Sample> trainingSet = Utils.getTrainingSetFromZip(args[argsAsList.lastIndexOf("-csv") + 1]);
            Utils.dataSetToCSV(trainingSet, args[argsAsList.lastIndexOf("-csv") + 2]);
        } else if (argsAsList.contains("-arff")) {
            List<Sample> trainingSet = Utils.getTrainingSetFromZip(args[argsAsList.lastIndexOf("-arff") + 1]);
            Utils.dataSetToARFF(trainingSet, args[argsAsList.lastIndexOf("-arff") + 2]);
        } else if (argsAsList.contains("-verifyModel")) {
            List<Sample> trainingSet = Utils.getTrainingSetFromZip(args[argsAsList.lastIndexOf("-verifyModel") + 1]);
            String model = args[argsAsList.lastIndexOf("-verifyModel") + 2];
            Classifier classifier = new SVMClassifier(model);
            testModel(trainingSet, classifier);
        } else {
            System.out.println("Unknown argument");
        }
        System.exit(0);
    }

    private static void testModel(List<Sample> trainingSet, Classifier classifier) {
        int correct = 0;
        for (Sample sample : trainingSet) {
            try {
                String output = classifier.test(normalize(sample));
                if (sample.getLabel().equals(output)) {
                    correct++;
                } else {
                    System.out.println("Sample: " + sample.getLabel() + " | Classified as: " + output);
                }
            } catch (Exception e) {
                System.out.println("Classifier threw an exception: " + e.getMessage());
            }
        }
        System.out.println("\nModel Accuracy Rate: " + (correct * 100.0) / trainingSet.size() + "%");
    }

    private static void test(AbstractFeatureExtractor fe, String testFile, String trainigSetZipFile) throws Exception {
        File modelFile = new File(Constants.CLASSIFIER_MODEL_URL);
        if (Utils.isSourceUrl(Constants.CLASSIFIER_MODEL_URL)) {
            modelFile = new File(Utils.getFileNameFromUrl(trainigSetZipFile));
            if (!modelFile.exists()) {
                Utils.get(Constants.CLASSIFIER_MODEL_URL);
            }
        }
        Classifier classifier = new MultiLayerPerceptronClassifier(new FileInputStream(modelFile));
        Sample sample = fe.extract(testFile);
        sample = normalize(sample);
        System.out.println("got the word: " +
                classifier.test(sample));
    }

    private static void dataset(AbstractFeatureExtractor fe, String folderPath) throws Exception {
        File samplesDir = new File(folderPath);
        for (String sampleName : samplesDir.list()) {
            File sampleFile = new File(samplesDir.getAbsolutePath() + "/" + sampleName);
            if (sampleFile.isFile() && sampleFile.getName().contains("MOV")) {
                Sample sample = fe.extract(sampleFile.getAbsolutePath());
                sample = normalize(sample);
                XStream.write(sampleName.split("\\.")[0] + ".xml", sample);
            }
        }
    }

    private static Sample normalize(Sample sample, Normalizer... normalizers) {
        if (sample.getOriginalMatrixSize() == 0) {
            sample.setOriginalMatrixSize(sample.getMatrix().size());
        }
        for (Normalizer normalizer : normalizers) {
            sample = normalizer.normalize(sample);
        }
        return sample;
    }

    /**
     * Normalize sample using predefined normalizers
     *
     * @param sample the sample to normalize
     * @return the normalized sample
     */
    public static Sample normalize(Sample sample) {
        Normalizer sfn = new SkippedFramesNormalizer(),
                cn = new CenterNormalizer(),
                tn = new LinearStretchTimeNormalizer();
        return normalize(sample, sfn, cn, tn);
    }

}
