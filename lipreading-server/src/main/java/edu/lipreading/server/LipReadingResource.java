
package edu.lipreading.server;

import edu.lipreading.LipReading;
import edu.lipreading.Sample;
import edu.lipreading.classification.Classifier;
import edu.lipreading.classification.MultiLayerPerceptronClassifier;
import edu.lipreading.normalization.CenterNormalizer;
import edu.lipreading.normalization.LinearStretchTimeNormalizer;
import edu.lipreading.normalization.Normalizer;
import edu.lipreading.normalization.SkippedFramesNormalizer;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

// The Java class will be hosted at the URI path "/lipreading"
@Path("/lipreading")
public class LipReadingResource {
    private Classifier classifier;
    private Normalizer cn = new CenterNormalizer();
    private Normalizer tn = new LinearStretchTimeNormalizer();
    private Normalizer sfn = new SkippedFramesNormalizer();
    private AtomicInteger counter = new AtomicInteger(0);
    private Map<Integer, Sample> instances = new HashMap<Integer, Sample>();


    public LipReadingResource() {
        try {
            classifier = new MultiLayerPerceptronClassifier(new URL("https://dl.dropbox.com/u/8720454/test3/yesnohello2.model").openStream());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
    // TODO: update the class to suit your needs
    
    // The Java method will process HTTP GET requests
    @GET 
    // The Java method will produce content identified by the MIME Media
    // type "text/plain"
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        return "Got it!";
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public SampleJson classify(SampleJson sample) {
        return sample;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String classify(Sample sample){
        int id = counter.getAndIncrement();
        instances.put(id, sample);
        return classifier.test(LipReading.normelize(sample, sfn, cn, tn)) + ", " + id;
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public String train(int id , String label){
        Sample sample = instances.remove(id);
        sample.setLabel(label);
        //add as line arff file
        //addSampleToArff(sample);
        return "OK";
    }
}
