package edu.lipreading.classification;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.List;

import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import edu.lipreading.Constants;
import edu.lipreading.Sample;
import edu.lipreading.Utils;

public class MultiLayerPerceptronClassifier implements Classifier{
	private final static String[] ANS = {"hello", "no", "yes"};
	private MultilayerPerceptron classifier;

	public MultiLayerPerceptronClassifier(String modelFilePath) throws Exception {
		File modelFile = new File(Utils.getFileNameFromUrl(modelFilePath));
		if(!modelFile.exists())
			Utils.get(modelFilePath);
		this.classifier  = (MultilayerPerceptron)weka.core.SerializationHelper.read(new FileInputStream(modelFile));
	}

	public MultiLayerPerceptronClassifier() {
        // TODO Auto-generated constructor stub
    }

    @Override
	public String test(Sample test) {
		double ans = -1;
		try {
			Instance sampleToInstance = sampleToInstance(test);
			ans = classifier.classifyInstance(sampleToInstance);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ANS[(int)ans];
	}

	private Instance sampleToInstance(Sample sample) {
		Instance instance = new DenseInstance(801);
		instance.setMissing(0);
		for (int i = 0; i < 800; i++) {
			instance.setValue(i + 1, sample.getMatrix().get(i / 8).get(i % 8));
		}
		return instance;
	}


	@Override
	public void train(List<Sample> trainingSet) {
	    ArffLoader loader = new ArffLoader();
	    try {
            loader.setSource(new URL(Constants.DEFAULT_ARFF_FILE));
            Instances dataSet = loader.getDataSet();
            dataSet.setClassIndex(0);
            this.classifier = new MultilayerPerceptron();
            classifier.buildClassifier(dataSet);
            weka.core.SerializationHelper.write("mp-classifier.model", classifier);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	@Override
	public void update(Sample train) {
		// TODO Auto-generated method stub
	}
	
	public static void main(String ...args) throws Exception {
	    new MultiLayerPerceptronClassifier().train(null);
	}

}
