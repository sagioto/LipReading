
package edu.lipreading.server;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.header.MediaTypes;
import junit.framework.TestCase;
import org.glassfish.grizzly.http.server.HttpServer;


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
     * Test to see that the message "Got it!" is sent in the response.
     */
    public void testMyResource() {
        String responseMsg = r.path("/lipreading").get(String.class);
        assertEquals("Got it!", responseMsg);
    }

    /**
     * Test classification
     */
    public void testClassify() {
        SampleJson sample = new SampleJson();
        sample.setHeight(600);
        sample.setWidth(800);
        sample.setId("test_sample_1");
        sample.setLabel("Hello");
        sample.setMatrix(new byte[][]{});
        sample.setOriginalMatrixSize(0);

        SampleJson response = r.path("/lipreading").post(SampleJson.class, sample);
        assertEquals(sample.getId(), response.getId());
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
