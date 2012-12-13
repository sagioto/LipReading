package edu.lipreading.classification;

import java.io.File;
import java.util.List;

import weka.classifiers.misc.SerializedClassifier;
import weka.core.DenseInstance;
import weka.core.Instance;
import edu.lipreading.Sample;

public class MultilayerPerceptronClassifier implements Classifier{
	private final static String[] ANS = {"hello", "no", "yes"};
	private final static SerializedClassifier serializedClassifier = new SerializedClassifier();
	
	
	
	@Override
	public String classify(List<Sample> trainingSet, Sample test) {
		double ans = -1;
		serializedClassifier.setModelFile(new File("nn2.model"));
		weka.classifiers.Classifier currentModel = serializedClassifier.getCurrentModel();
		try {
			ans = currentModel.classifyInstance(sampleToInstance(test));
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

}
