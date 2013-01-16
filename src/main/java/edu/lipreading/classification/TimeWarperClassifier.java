package edu.lipreading.classification;

import java.util.List;

import edu.lipreading.Constants;
import edu.lipreading.Parallel;
import edu.lipreading.Sample;
import edu.lipreading.Utils;

public class TimeWarperClassifier implements Classifier{
    private List<Sample> trainingSet;
    private List<String> vocabulary = Constants.VOCABULARY;


    @Override
    public String test(final Sample test) {
        final double [] results = new double[vocabulary.size()];
        final int [] counts = new int[vocabulary.size()];
        final TimeWarper tw = new TimeWarper();
        Parallel.For(trainingSet, 
                new Parallel.Operation<Sample>() {
            public void perform(Sample training) {
                if(!test.equals(training)){
                    for (int i = 0; i < vocabulary.size(); i++) {
                        if(training.getId().contains(vocabulary.get(i))){
                            results[i] += tw.dtw(test, training);
                            counts[i]++;
                        }
                    }
                }
            };
        });
        for (int i = 0; i < vocabulary.size(); i++) {
            results[i] /= counts[i];
        }
        int minIndex = Utils.getMinIndex(results);
        return vocabulary.get(minIndex);
    }


    @Override
    public void train(List<Sample> trainingSet) {
        this.trainingSet = trainingSet;

    }


    @Override
    public void update(Sample train) {
        this.trainingSet.add(train);
    }


}
