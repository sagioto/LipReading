package edu.lipreading.server;

import edu.lipreading.LipReading;
import edu.lipreading.Sample;
import edu.lipreading.classification.Classifier;
import edu.lipreading.classification.MultiLayerPerceptronClassifier;
import edu.lipreading.normalization.CenterNormalizer;
import edu.lipreading.normalization.LinearStretchTimeNormalizer;
import edu.lipreading.normalization.Normalizer;
import edu.lipreading.normalization.SkippedFramesNormalizer;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: Sagi
 * Date: 28/03/13
 * Time: 00:09
 */
public class LipReadingContext {
    private static final int TRAIN_EACH = 100;
    private static final Logger log = Logger.getLogger(new Object(){}.getClass().getEnclosingClass().getSimpleName());
    private static Classifier classifier;
    static {
        try {
            log.info("starting MLP classifier...");
            InputStream modelFileInputStream = new URL("https://dl.dropbox.com/u/8720454/test3/yesnohello2.model").openStream();
            classifier = new MultiLayerPerceptronClassifier(modelFileInputStream);
            modelFileInputStream.close();
            log.info("finished reading model file");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final AtomicInteger counter = new AtomicInteger(0);
    private static final ConcurrentMap<Integer, Sample> instances = new ConcurrentHashMap<Integer, Sample>();

    public static Sample normalize(Sample sample){
        if(log.isLoggable(Level.FINEST))
            log.finest("normalizing sample with sample id:" + sample.getId());
        return LipReading.normalize(sample);
    }

    public static String classify(Sample sample){
        if(log.isLoggable(Level.FINEST))
            log.finest("classifying sample with sample id:" + sample.getId());
        return classifier.test(sample);
    }

    public static int put(Sample sample){
        int count = count();
        instances.put(count, sample);
        if((count % TRAIN_EACH == 0) && (count != 0)){
            startTraining();
        }
        if(log.isLoggable(Level.FINEST))
            log.finest("adding sample with id:" + count + " to training set");
        return count;
    }

    public static void startTraining() {

        new Thread(new Runnable(){
            @Override
            public void run() {
                log.info("starting classifier training with " + instances.size() + " samples in training set...");
                classifier.train(new Vector<Sample>(instances.values()));
                log.info("finished classifier training");
            }
        }).start();
    }

    public static Sample get(int id){
        if(log.isLoggable(Level.FINEST))
            log.finest("getting sample with id:" + id+ " to training set");
        return instances.get(id);
    }

    private static int count(){
        return counter.getAndIncrement();
    }

    public static Sample remove(int id) {
        if(log.isLoggable(Level.FINEST))
            log.finest("removing sample with id:" + id+ " to training set");
        return instances.remove(id);
    }

    public static Map<Integer, Sample> list(){
        return new HashMap<Integer, Sample>(instances);
    }
}
