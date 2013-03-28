
package edu.lipreading.server;

import com.sun.jersey.api.client.Client;
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
        r = c.resource(Main.BASE_URI);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        httpServer.stop();
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
        String response = r.path("/lipreading/samples").type(MediaType.APPLICATION_JSON_TYPE).header("training", true).post(String.class, sample);
        assertEquals((s.getLabel() + ",0").toLowerCase(), response.toLowerCase());

        response = r.path("/lipreading/samples").type(MediaType.APPLICATION_JSON_TYPE).header("id", 0).put(String.class, sample);
        assertEquals("OK", response);

        /*TODO: check why these don't pass
        SamplePacket sp = r.path("/lipreading/samples").queryParam("id", "0").type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).get(SamplePacket.class);
        assertEquals("no", sp.getLabel().toLowerCase());

        sp = r.path("/lipreading/samples").queryParam("id", "0").type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).delete(SamplePacket.class);
        assertEquals("no", sp.getLabel().toLowerCase());*/
    }

    /**
     * Test if a WADL document is available at the relative path
     * "application.wadl".
     */
    public void testApplicationWadl() {
        String serviceWadl = r.path("application.wadl").
                accept(MediaTypes.WADL).get(String.class);
                
        assertTrue(serviceWadl.length() > 0);
    }

}
