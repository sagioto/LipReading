
package edu.lipreading.server;

import edu.lipreading.Sample;
import edu.lipreading.SamplePacket;
import edu.lipreading.Utils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.logging.Logger;


@Path("/lipreading/samples")
public class SampleResource {
    private static final Logger log = Logger.getLogger(new Object(){}.getClass().getEnclosingClass().getSimpleName());

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public SamplePacket get(@PathParam("id")int id) {
        log.info("got get request with id: " + id);
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

    @DELETE
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public SamplePacket remove(@PathParam("id")int id) {
        return Utils.getPacketFromSample(LipReadingContext.remove(id));
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String startTraining(){
        LipReadingContext.startTraining();
        return "OK";
    }
}
