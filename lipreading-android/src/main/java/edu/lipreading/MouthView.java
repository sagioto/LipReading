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
import static com.googlecode.javacv.cpp.opencv_imgproc.cvGetQuadrangleSubPix;

public class MouthView extends View implements Camera.PreviewCallback {
    public static final int SUBSAMPLING_FACTOR = 4;
    private LipReadingActivity context;
    private IplImage image;
    private List<Integer> points;

    public MouthView(LipReadingActivity context) throws IOException {
        super(context);
        this.context = context;
    }

    public void onPreviewFrame(final byte[] data, final Camera camera) {
        try {
            Camera.Size size = camera.getParameters().getPreviewSize();
            processImage(data, size.width, size.height);
            camera.addCallbackBuffer(data);
        } catch (RuntimeException e) {
            // The camera has probably just been released, ignore.
        }
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
            IplImage rotated = rotateImage(image, 90);
            points = context.getFeatureExtractor().getPoints(rotated);
            context.getSample().getMatrix().add(points);
        } catch (Exception e) {
            e.printStackTrace();
        }
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setTextSize(20);


        if (points != null) {
            paint.setStrokeWidth(2);
            paint.setStyle(Paint.Style.STROKE);
            float scaleX = (float)getWidth()/image.width();
            float scaleY = (float)getHeight()/image.height();

            for (int i = 0; i < points.size(); i += 2) {
                canvas.drawCircle(points.get(i) * scaleX, points.get(i + 1) * scaleY, 3, paint);
            }
        }
    }

    private IplImage rotateImage(final IplImage src, float angleDegrees)
    {
        // Create a map_matrix, where the left 2x2 matrix
        // is the transform and the right 2x1 is the dimensions.
        float[] m = new float[6];
        CvMat M = CvMat.create(2, 3, CV_32F);
        int w = src.width();
        int h = src.height();
        float angleRadians = angleDegrees * ((float)Math.PI / 180.0f);
        m[0] = (float)( Math.cos(angleRadians) );
        m[1] = (float)( Math.sin(angleRadians) );
        m[3] = -m[1];
        m[4] = m[0];
        m[2] = w*0.5f;
        m[5] = h*0.5f;
        M.put(0, m[0]);
        M.put(1, m[1]);
        M.put(2, m[2]);
        M.put(3, m[3]);
        M.put(4, m[4]);
        M.put(5, m[5]);

        // Make a spare image for the result
        CvSize sizeRotated = new CvSize();
        sizeRotated.width(Math.round(w));
        sizeRotated.height(Math.round(h));

        // Rotate
        IplImage imageRotated = cvCreateImage( sizeRotated, src.depth(), src.nChannels());

        // Transform the image
        cvGetQuadrangleSubPix(src, imageRotated, M);

        return imageRotated;
    }
}
