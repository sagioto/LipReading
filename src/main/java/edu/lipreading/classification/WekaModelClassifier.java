package edu.lipreading.classification;

import java.io.File;
import java.util.List;

import weka.classifiers.misc.SerializedClassifier;
import weka.core.DenseInstance;
import weka.core.Instance;
import edu.lipreading.Sample;
import edu.lipreading.Utils;

public class WekaModelClassifier implements Classifier{
	private final static String[] ANS = {"hello", "no", "yes"};
	private final SerializedClassifier serializedClassifier = new SerializedClassifier();

	public WekaModelClassifier(String modelFilePath) throws Exception {
		File modelFile = new File(Utils.getFileNameFromUrl(modelFilePath));
		if(!modelFile.exists())
			Utils.get(modelFilePath);
		this.serializedClassifier.setModelFile(modelFile);
	}

	@Override
	public String test(Sample test) {
		double ans = -1;
		try {
			System.out.println("get current model");
			weka.classifiers.Classifier currentModel = serializedClassifier.getCurrentModel();
			System.out.println("turn sample to instance");
			Instance sampleToInstance = sampleToInstance(test);
			System.out.println("classify");
			ans = currentModel.classifyInstance(sampleToInstance);
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
		// TODO Auto-generated method stub
	}

	@Override
	public void update(Sample train) {
		// TODO Auto-generated method stub
	}

}
