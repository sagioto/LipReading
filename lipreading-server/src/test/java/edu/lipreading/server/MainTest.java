
package edu.lipreading.server;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.header.MediaTypes;
import edu.lipreading.Sample;
import edu.lipreading.SamplePacket;
import edu.lipreading.Utils;
import junit.framework.TestCase;
import org.glassfish.grizzly.http.server.HttpServer;
import weka.core.xml.XStream;

import javax.ws.rs.core.MediaType;
import java.net.URL;


public class MainTest extends TestCase {

    private HttpServer httpServer;

    private WebResource r;

    public MainTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        //start the Grizzly2 web container
        httpServer = Main.startServer();

        // create the client
        Client c = Client.create();
        r = c.resource(Main.BASE_URI/*"http://lip-reading.appspot.com/"*/);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        httpServer.stop();
    }

    /**
     * Test to see server is loading.
     * Test to see that the message "Got it!" is sent in the response.
     */
    public void testLipReadingResource() {
        String responseMsg = r.path("/lipreading").get(String.class);
        assertEquals("Got it!", responseMsg);
    }

    /**
     * Test classification
     */
    public void testClassify() {
        SamplePacket sample = new SamplePacket();
        Sample s = null;
        try {
            s = (Sample) XStream.read(new URL("https://dl.dropbox.com/u/7091414/No31-18.29.10-24.02.2013.xml").openStream());
            sample = Utils.getPacketFromSample(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try{
        String response = r.path("samples").type(MediaType.APPLICATION_JSON_TYPE).header("training", true).post(String.class, sample);
//        assertEquals((s.getLabel() + ",0").toLowerCase(), response.toLowerCase());
        int id = Integer.parseInt(response.split(",")[1]);

        response = r.path("/samples/" + id).type(MediaType.APPLICATION_JSON_TYPE).put(String.class, sample.getLabel());
//        assertEquals("OK", response);

        SamplePacket sp = r.path("/samples/" + id).get(SamplePacket.class);
//        assertEquals("no", sp.getLabel().toLowerCase());

        sp = r.path("/samples/" + id).type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).delete(SamplePacket.class);
//        assertEquals("no", sp.getLabel().toLowerCase());
        }
        catch (UniformInterfaceException e){
            e.printStackTrace();
//            assertFalse("got exception", true);
        }
    }

    public void testFalseGet() {
        try{
            r.path("/samples/" + 0).get(SamplePacket.class);
//            assertFalse("should have got exception", true);
        } catch (UniformInterfaceException e){
//            assertEquals(404, e.getResponse().getStatus());
        }
    }


    public void testFalseTrain() {
        try{
            r.path("/samples/" + 0)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .put(String.class, "something");
//            assertFalse("should have got exception", true);
        } catch (UniformInterfaceException e){
//            assertEquals(404, e.getResponse().getStatus());
        }
    }

    public void testFalseRemove() {
        try{
            r.path("/samples/" + 0)
                    .type(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .delete(SamplePacket.class);
//            assertFalse("should have got exception", true);
        } catch (UniformInterfaceException e){
//            assertEquals(404, e.getResponse().getStatus());
        }
    }


    /**
     * Test if a WADL document is available at the relative path
     * "application.wadl".
     */
    public void testApplicationWadl() {
        String serviceWadl = r.path("application.wadl").
                accept(MediaTypes.WADL).get(String.class);
                
//        assertTrue(serviceWadl.length() > 0);
    }

}
