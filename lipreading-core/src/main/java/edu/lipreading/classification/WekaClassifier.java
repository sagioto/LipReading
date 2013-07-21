package edu.lipreading.classification;

import edu.lipreading.Constants;
import edu.lipreading.Sample;
import weka.classifiers.AbstractClassifier;
import weka.core.*;
import weka.core.converters.ArffLoader;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An abstract super-type wrapper for Weka classifiers. Provides all the basic Classifier functionality for Weka classifiers.
 * Created with IntelliJ IDEA.
 * User: Dagan
 * Date: 24/05/13
 * Time: 20:17
 */
public abstract class WekaClassifier implements Classifier {
    private static final int INSTANCE_SIZE = (Constants.FRAMES_COUNT * Constants.POINT_COUNT * 2) + Constants.SAMPLE_ROW_SHIFT;
    protected List<String> vocabulary = Constants.VOCABULARY;
    protected AbstractClassifier classifier;
    private List<Sample> samples;

    public WekaClassifier() throws Exception {}

    public WekaClassifier(InputStream modelFileInputStream) throws Exception {
        Object read = SerializationHelper.read(modelFileInputStream);
        this.classifier = (AbstractClassifier) read;
    }

    public WekaClassifier(String modelFile) throws Exception {
        Object read = SerializationHelper.read(modelFile);
        this.classifier = (AbstractClassifier) read;
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
        return vocabulary.get((int) ans);
    }

    protected Instance sampleToInstance(Sample sample) {
        Instance instance = new DenseInstance(INSTANCE_SIZE);
        instance.setMissing(0);
        instance.setValue(1, sample.getOriginalMatrixSize());
        instance.setValue(2, sample.getWidth());
        instance.setValue(3, sample.getHeight());
        for (int i = 0; i < INSTANCE_SIZE - Constants.SAMPLE_ROW_SHIFT; i++) {
            instance.setValue(i + Constants.SAMPLE_ROW_SHIFT, sample.getMatrix().get(i / 8).get(i % 8));
        }
        return instance;
    }

    public void trainFromFile(String arffFilePath) throws Exception {
        ArffLoader loader = new ArffLoader();
        loader.setSource(new File(arffFilePath).toURI().toURL());
        Instances dataSet = loader.getDataSet();
        dataSet.setClassIndex(0);
        AbstractClassifier c = getNewClassifierInstance();
        c.buildClassifier(dataSet);
        classifier = c;
        weka.core.SerializationHelper.write("mp-classifier.model", classifier);
    }

    public List<String> getVocabulary() {
        return vocabulary;
    }

    public void setVocabulary(List<String> vocabulary) {
        this.vocabulary = vocabulary;
    }

    @Override
    public void update(Sample train) {
        this.samples.add(train);
        this.train(samples);
    }

    @Override
    public void train(List<Sample> trainingSet) {
        this.samples = trainingSet;
        ArrayList<Attribute> list = Collections.list(sampleToInstance(trainingSet.get(0)).enumerateAttributes());
        Instances instances = new Instances("dataset", list, trainingSet.size());
        for (Sample sample : trainingSet) {
            instances.add(sampleToInstance(sample));
        }
        try {
            AbstractClassifier c = getNewClassifierInstance();
            c.buildClassifier(instances);
            classifier = c;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void saveToFile(String file) {
        Debug.saveToFile(file, classifier);
    }

    protected abstract AbstractClassifier getNewClassifierInstance();
}
