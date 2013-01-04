package edu.lipreading.classification;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.List;

import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ArffLoader;
import edu.lipreading.Constants;
import edu.lipreading.Sample;
import edu.lipreading.Utils;

public class MultiLayerPerceptronClassifier implements Classifier{
	private final static List<String> ANS = Constants.VOCABULARY;
	private MultilayerPerceptron classifier;
	private List<Sample> samples;
	
	public MultiLayerPerceptronClassifier(String modelFilePath) throws Exception {
		File modelFile = new File(Utils.getFileNameFromUrl(modelFilePath));
		if(!modelFile.exists())
			Utils.get(modelFilePath);
		this.classifier  = (MultilayerPerceptron)SerializationHelper.read(new FileInputStream(modelFile));
	}

	@Override
	public String test(Sample test) {
		double ans = -1;
		try {
			Instance sampleToInstance = sampleToInstance(test);
			ans = classifier.classifyInstance(sampleToInstance);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return ANS.get((int)ans);
	}

	private Instance sampleToInstance(Sample sample) {
		Instance instance = new DenseInstance(801);
		instance.setMissing(0);
		for (int i = 0; i < 800; i++) {
			instance.setValue(i + 1, sample.getMatrix().get(i / 8).get(i % 8));
		}
		return instance;
	}


	public void trainFromFile() throws Exception {
		ArffLoader loader = new ArffLoader();
			loader.setSource(new URL(Constants.DEFAULT_ARFF_FILE));
			Instances dataSet = loader.getDataSet();
			dataSet.setClassIndex(0);
			this.classifier = new MultilayerPerceptron();
			classifier.buildClassifier(dataSet);
			weka.core.SerializationHelper.write("mp-classifier.model", classifier);
	}

	@Override
	public void update(Sample train) {
		this.samples.add(train);
		this.train(samples);
	}

	@Override
	public void train(List<Sample> trainingSet) {
		this.samples = trainingSet;
		Instances instances = new Instances("dataset", null, trainingSet.size());
		for (Sample sample : trainingSet) {
			instances.add(sampleToInstance(sample));
		}
		this.classifier = new MultilayerPerceptron();
		try {
			classifier.buildClassifier(instances);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
