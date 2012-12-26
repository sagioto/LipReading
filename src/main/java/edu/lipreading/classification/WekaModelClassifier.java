package edu.lipreading.classification;

import java.io.File;
import java.util.List;

import weka.classifiers.misc.SerializedClassifier;
import weka.core.DenseInstance;
import weka.core.Instance;
import edu.lipreading.Sample;
import edu.lipreading.Utils;

public class WekaModelClassifier implements Classifier{
	public final static  String MPC_MODEL_URL = "https://dl.dropbox.com/u/8720454/weka/mp-10folds.model";
	private final static String[] ANS = {"hello", "no", "yes"};
	private final static SerializedClassifier serializedClassifier = new SerializedClassifier();



	@Override
	public String test(Sample test) {
		double ans = -1;
		try {
			File modelFile = new File(Utils.getFileNameFromUrl(MPC_MODEL_URL));
			if(!modelFile.exists())
				Utils.get(MPC_MODEL_URL);
			serializedClassifier.setModelFile(modelFile);
			weka.classifiers.Classifier currentModel = serializedClassifier.getCurrentModel();
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


	@Override
	public void train(List<Sample> trainingSet) {
		// TODO Auto-generated method stub
	}

	@Override
	public void update(Sample train) {
		// TODO Auto-generated method stub
	}

}
