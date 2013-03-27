
package edu.lipreading.server;

import edu.lipreading.Sample;
import edu.lipreading.SamplePacket;
import edu.lipreading.Utils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

// The Java class will be hosted at the URI path "/lipreading"
@Path("/lipreading/samples/")
public class SampleResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public SamplePacket get(@PathParam("id")int id) {
        return Utils.getPacketFromSample(LipReadingContext.get(id));
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String classify(@HeaderParam("training") boolean training, SamplePacket sp) {
        Sample sample = Utils.getSampleFromPacket(sp);
        int id = -1;
        if(training) {
            id = LipReadingContext.put(sample);
        }
        return LipReadingContext.classify(LipReadingContext.normalize(sample)) + "," + id;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String train(@HeaderParam("id")int id , String label){
        LipReadingContext.get(id).setLabel(label);
        return "OK";
    }

    /*
    TODO: find a way to also post an already labeled and normalized sample like in our xmls
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public int addToTrainingSet(SamplePacket sp){
        return LipReadingContext.put(Utils.getSampleFromPacket(sp));
    }*/

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String startTraining(){
        LipReadingContext.startTraining();
        return "OK";
    }
}
