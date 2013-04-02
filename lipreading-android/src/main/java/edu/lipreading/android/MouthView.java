package edu.lipreading.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.view.View;
import com.googlecode.javacv.cpp.opencv_core.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import static com.googlecode.javacv.cpp.opencv_core.*;

public class MouthView extends View implements Camera.PreviewCallback {
    public static final int SUBSAMPLING_FACTOR = 2;

    private Context context;
    private IplImage image;
    private List<Integer> points;
    private float scaleX;
    private float scaleY;

    private Paint paint;
    private int imageWidth;
    private int imageHeight;
    private int dataStride;
    private int imageStride;
    /*private ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread("Feature-Extractor-Driver");
            thread.setDaemon(true);
            return thread;
        }
    });*/


    public MouthView(Context context) throws IOException {
        super(context);
        this.context = context;
        this.paint = new Paint();
        this.paint.setAntiAlias(true);
    }

    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {
        processImageNoSkip(data, 352, 288);
        camera.addCallbackBuffer(data);
        /*executor.submit(new featureExtractorDriver(data, camera));*/
    }

    protected void processImage(byte[] data, int width, int height) {
        int f = SUBSAMPLING_FACTOR;
        if (image == null || image.width() != width / f || image.height() != height / f) {
            image = IplImage.create(width / f, height / f, IPL_DEPTH_8U, 3);
            imageWidth = image.width();
            imageHeight = image.height();
            dataStride = f * width;
            imageStride = image.widthStep();
        }
        ByteBuffer imageBuffer = image.getByteBuffer();
        for (int y = 0; y < imageHeight; y++) {
            int dataLine = y * dataStride;
            int imageLine = y * imageStride;
            for (int x = 0; x < imageWidth; x++) {
                imageBuffer.put(imageLine + x, data[dataLine + f * x]);
            }
        }
        try {
            IplImage rotated = rotateImage(image);
            if((scaleX == 0 || scaleY == 0) && rotated != null){
                scaleX = (float)getWidth() / rotated.width();
                scaleY = (float)getHeight() / rotated.height();
            }
            LipReadingActivity lrActivity = (LipReadingActivity)context;
            points = lrActivity.getFeatureExtractor().getPoints(rotated);
            if(lrActivity.isRecording())
                lrActivity.getSample().getMatrix().add(points);
            postInvalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void processImageNoSkip(byte[] data, int width, int height) {
        if (image == null || image.width() != width || image.height() != height) {
            image = IplImage.create(width , height, 12, 3);
            imageWidth = image.width();
            imageHeight = image.height();
            dataStride = width;
            imageStride = image.widthStep();
        }

        ByteBuffer imageBuffer = image.getByteBuffer();
        imageBuffer.put(data);
        try {
            IplImage rotated = rotateImage(image);
            if((scaleX == 0 || scaleY == 0) && rotated != null){
                scaleX = (float)getWidth() / rotated.width();
                scaleY = (float)getHeight() / rotated.height();
            }
            LipReadingActivity lrActivity = (LipReadingActivity)context;
            points = lrActivity.getFeatureExtractor().getPoints(rotated);
            if(lrActivity.isRecording())
                lrActivity.getSample().getMatrix().add(points);
            postInvalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    protected void processImageNoRotate(byte[] data, int width, int height) {
        int f = 1;
        if (image == null || image.width() != width / f || image.height() != height / f) {
            image = IplImage.create(height / f, width / f, 12, 3);
            imageWidth = image.width();
            imageHeight = image.height();
            dataStride = f * width;
            imageStride = image.widthStep();
        }
        ByteBuffer imageBuffer = image.getByteBuffer();
        for (int y = 0; y < imageHeight; y++) {
            int dataLine = y * dataStride;
            int imageLine = y * imageStride;
            for (int x = 1; x <= imageWidth; x++) {
                imageBuffer.put(imageLine + x - 1, data[((height - x) * width) + y]);
            }
        }
        try {

            if((scaleX == 0 || scaleY == 0) && image != null){
                scaleX = (float)getWidth() / image.width();
                scaleY = (float)getHeight() / image.height();
            }
            LipReadingActivity lrActivity = (LipReadingActivity)context;
            points = lrActivity.getFeatureExtractor().getPoints(image);
            if(lrActivity.isRecording())
                lrActivity.getSample().getMatrix().add(points);
            postInvalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        LipReadingActivity lrActivity = (LipReadingActivity)context;
        if(lrActivity.isRecording()){
            paint.setColor(Color.RED);
            canvas.drawCircle(20, 20, 10, paint);
        }
        paint.setColor(Color.GREEN);
        //List<Integer> points = Arrays.asList(100, 150, 57, 254, 91, 240, 91, 282);
        if (points != null && image != null) {
            paint.setStyle(Paint.Style.FILL);
            for (int i = 0; i < points.size(); i += 2) {
                switch (i){
                    case 0: paint.setColor(Color.GREEN); break;
                    case 2: paint.setColor(Color.RED); break;
                    case 4: paint.setColor(Color.YELLOW); break;
                    case 6: paint.setColor(Color.BLUE); break;
                }
                canvas.drawCircle(getWidth() - (points.get(i) * scaleX), points.get(i + 1) * scaleY, 3, paint);
            }
        }
    }

    private IplImage rotateImage(final IplImage src)
    {
        IplImage dst = cvCreateImage(new CvSize(src.height(), src.width()), src.depth(), src.nChannels());
        cvTranspose(src, dst);
        cvFlip(dst, dst, 0);
        return dst;
    }

    private class featureExtractorDriver implements Runnable {
        private final byte[] data;
        private final Camera camera;

        public featureExtractorDriver(byte[] data, Camera camera) {
            this.data = data;
            this.camera = camera;
        }

        @Override
        public void run() {
            try {
                //long start = System.currentTimeMillis();
                //Camera.Size size = camera.getParameters().getPreviewSize();
                processImage(data, 352, 288);
                camera.addCallbackBuffer(data);
                //Log.d("processing took-", "" + (System.currentTimeMillis() - start));
            } catch (RuntimeException e) {
                // The camera has probably just been released, ignore.
            }
        }
    }
}
