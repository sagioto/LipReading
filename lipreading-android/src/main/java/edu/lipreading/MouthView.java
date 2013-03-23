package edu.lipreading;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.view.View;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import static com.googlecode.javacv.cpp.opencv_core.*;

public class MouthView extends View implements Camera.PreviewCallback {
    public static final int SUBSAMPLING_FACTOR = 2;
    private LipReadingActivity context;
    private IplImage image;
    private List<Integer> points;
    private float scaleX;
    private float scaleY;

    public MouthView(LipReadingActivity context) throws IOException {
        super(context);
        this.context = context;
    }

    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {
        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    Camera.Size size = camera.getParameters().getPreviewSize();
                    processImage(data, size.width, size.height);
                    camera.addCallbackBuffer(data);
                } catch (RuntimeException e) {
                    // The camera has probably just been released, ignore.
                }
            }
        }).start();
    }

    protected void processImage(byte[] data, int width, int height) {
        int f = SUBSAMPLING_FACTOR;
        if (image == null || image.width() != width/f || image.height() != height/f) {
            image = IplImage.create(width/f, height/f, IPL_DEPTH_8U, 3);
        }
        int imageWidth  = image.width();
        int imageHeight = image.height();
        int dataStride = f*width;
        int imageStride = image.widthStep();
        ByteBuffer imageBuffer = image.getByteBuffer();
        for (int y = 0; y < imageHeight; y++) {
            int dataLine = y*dataStride;
            int imageLine = y*imageStride;
            for (int x = 0; x < imageWidth; x++) {
                imageBuffer.put(imageLine + x, data[dataLine + f*x]);
            }
        }
        try {
            IplImage rotated = rotateImage(image);
            points = context.getFeatureExtractor().getPoints(rotated);
            if(context.isRecording())
                context.getSample().getMatrix().add(points);
            postInvalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if((scaleX == 0 || scaleY == 0) && image != null){
            //switch between height and width since we use the rotated image
            scaleX = (float)getWidth()/image.height();
            scaleY = (float)getHeight()/image.width();
        }
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        if(context.isRecording()){
            paint.setColor(Color.RED);
            canvas.drawCircle(20, 20, 10, paint);
        }
        paint.setColor(Color.GREEN);
        //int [] arr = new int[] {125, 272, 57, 254, 91, 240, 91, 282};
        if (points != null && image != null) {
            paint.setStyle(Paint.Style.FILL);
            for (int i = 0; i < points.size(); i += 2) {
                canvas.drawCircle(points.get(i) * scaleX, points.get(i + 1) * scaleY, 2, paint);
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
}
