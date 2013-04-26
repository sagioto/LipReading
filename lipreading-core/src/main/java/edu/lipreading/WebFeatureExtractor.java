package edu.lipreading;

import com.googlecode.javacpp.BytePointer;
import com.googlecode.javacv.cpp.opencv_core;
import edu.lipreading.vision.AbstractFeatureExtractor;
import edu.lipreading.vision.NoMoreStickersFeatureExtractor;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketHandler;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.logging.Logger;

import static com.googlecode.javacv.cpp.opencv_core.CV_8UC1;
import static com.googlecode.javacv.cpp.opencv_core.cvMat;
import static com.googlecode.javacv.cpp.opencv_highgui.cvDecodeImage;

/**
 * Created with IntelliJ IDEA.
 * User: Sagi
 * Date: 25/04/13
 * Time: 21:47
 */
public class WebFeatureExtractor extends Server {

    private final static Logger LOG = Logger.getLogger(WebFeatureExtractor.class.getSimpleName());
    private final static AbstractFeatureExtractor fe = new NoMoreStickersFeatureExtractor();

    public WebFeatureExtractor(int port) {
        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(port);
        addConnector(connector);

        WebSocketHandler wsHandler = new WebSocketHandler() {
            public WebSocket doWebSocketConnect(HttpServletRequest request,	String protocol) {
                return new FeatureExtractorWebSocket();
            }
        };
        setHandler(wsHandler);
    }

    /**
     * Simple innerclass that is used to handle websocket connections.
     *
     * @author jos
     */
    private static class FeatureExtractorWebSocket implements WebSocket, WebSocket.OnBinaryMessage, WebSocket.OnTextMessage {

        private Connection connection;


        public FeatureExtractorWebSocket() {
            super();
        }

        /**
         * On open we set the connection locally, and enable
         * binary support
         */
        @Override
        public void onOpen(Connection connection) {
            LOG.info("got connection open");
            this.connection = connection;
            this.connection.setMaxBinaryMessageSize(1024 * 512);
        }

        /**
         * Cleanup if needed. Not used for this example
         */
        @Override
        public void onClose(int code, String message) {
            LOG.info("got connection closed");
        }

        /**
         * When we receive a binary message we assume it is an image. We then run this
         * image through our face detection algorithm and send back the response.
         */
        @Override
        public void onMessage(byte[] data, int offset, int length) {
            //LOG.info("got data message");
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            bOut.write(data, offset, length);
            try {
                String result = convert(bOut.toByteArray());
                this.connection.sendMessage(result);
            } catch (Exception e) {
                LOG.severe("Error in facedetection, ignoring message:" + e.getMessage());
            }
        }

        @Override
        public void onMessage(String data) {
            LOG.info("got string message");
        }
    }
    public static String convert(byte[] imageData) throws Exception {
        opencv_core.IplImage originalImage = cvDecodeImage(cvMat(1, imageData.length, CV_8UC1, new BytePointer(imageData)));
        List<Integer> points = fe.getPoints(originalImage);
        if(points == null)
            return "null";
        String ans = "";
        for (Integer point : points) {
            ans += point + ",";
        }
        return ans;
    }



    /**
     * Start the server on port 999
     */
    public static void main(String[] args) throws Exception {
        WebFeatureExtractor server = new WebFeatureExtractor(9999);
        server.start();
        server.join();
    }
}
