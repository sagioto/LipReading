package edu.lipreading;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;

import weka.core.xml.XStream;
import edu.lipreading.classification.Classifier;
import edu.lipreading.classification.MultiLayerPerceptronClassifier;
import edu.lipreading.normalization.CenterNormalizer;
import edu.lipreading.normalization.LinearStretchTimeNormalizer;
import edu.lipreading.normalization.Normalizer;
import edu.lipreading.vision.AbstractFeatureExtractor;
import edu.lipreading.vision.ColoredStickersFeatureExtractor;




public class LipReading {

    public static void main(String...args) throws Exception{
        List<String> argsAsList = Arrays.asList(args);
        if(args.length == 0 || argsAsList.contains("-help")){
            System.out.println("usage:");
            System.out.println("-help : prints this message");
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
        Normalizer tn = new LinearStretchTimeNormalizer();
        AbstractFeatureExtractor fe = new ColoredStickersFeatureExtractor();

        if(argsAsList.contains("-extract")){
            String sampleName = args[argsAsList.lastIndexOf("-extract") + 1];
            fe.setOutput(argsAsList.contains("-output"));
            XStream.write(sampleName.split("\\.")[0] + ".xml", cn.normalize(fe.extract(sampleName)));
        }
        else if(argsAsList.contains("-dataset")){
            dataset(fe, args[argsAsList.lastIndexOf("-dataset") + 1], cn, tn);
        }
        else if(argsAsList.contains("-test") && argsAsList.size() > argsAsList.lastIndexOf("-test") + 2){
            test(fe, args[argsAsList.lastIndexOf("-test") + 1], args[argsAsList.lastIndexOf("-test") + 2], cn, tn);
        }
        else if(argsAsList.contains("-test")){
            test(fe, args[argsAsList.lastIndexOf("-test") + 1], Constants.DEFAULT_TRAINING_SET_ZIP, cn, tn);
        }
        else if(argsAsList.contains("-csv")){
            List<Sample> trainingSet = Utils.getTrainingSetFromZip(args[argsAsList.lastIndexOf("-csv") + 1]);
            Utils.dataSetToCSV(trainingSet, args[argsAsList.lastIndexOf("-csv") + 2]);
        }
        else if(argsAsList.contains("-arff")){
            List<Sample> trainingSet = Utils.getTrainingSetFromZip(args[argsAsList.lastIndexOf("-arff") + 1]);
            Utils.dataSetToARFF(trainingSet, args[argsAsList.lastIndexOf("-arff") + 2]);
        }
        System.exit(0);
    }



    private static void test(AbstractFeatureExtractor fe, String testFile, String trainigSetZipFile, Normalizer... normalizers) throws Exception {
        File modelFile  = new File(Constants.MPC_MODEL_URL);
        if(Utils.isSourceUrl(Constants.MPC_MODEL_URL)){
            modelFile = new File(Utils.getFileNameFromUrl(trainigSetZipFile));
            if(!modelFile.exists()) {
                Utils.get(Constants.MPC_MODEL_URL);
            }
        }
        Classifier classifier = new MultiLayerPerceptronClassifier(new FileInputStream(modelFile));
        Sample sample = fe.extract(testFile);
        sample = normelize(sample, normalizers);
        System.out.println("got the word: " +
                classifier.test(sample));
    }

    private static void dataset(AbstractFeatureExtractor fe, String folderPath, Normalizer... normalizers) throws Exception {
        File samplesDir = new File(folderPath);
        for (String sampleName : samplesDir.list()) {
            File sampleFile = new File(samplesDir.getAbsolutePath()  + "/" + sampleName);
            if(sampleFile.isFile() && sampleFile.getName().contains("MOV")) {
                Sample sample = fe.extract(sampleFile.getAbsolutePath());
                sample = normelize(sample, normalizers);
                XStream.write(sampleName.split("\\.")[0] + ".xml", sample);
            }
        }
    }



    public static Sample normelize(Sample sample, Normalizer... normalizers) {
        for (Normalizer normalizer : normalizers) {
            sample = normalizer.normalize(sample);
        }
        return sample;
    }


}
