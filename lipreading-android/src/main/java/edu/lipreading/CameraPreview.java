package edu.lipreading;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.*;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Sagi
 * Date: 18/03/13
 * Time: 00:33
 *//*

class Preview extends SurfaceView implements SurfaceHolder.Callback {
    SurfaceHolder holder;
    Camera camera;
    Camera.PreviewCallback previewCallback;




    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
        Camera.Parameters parameters = camera.getParameters();

        */
/*List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        camera.setParameters(parameters);*//*

        if (previewCallback != null) {
            camera.setPreviewCallbackWithBuffer(previewCallback);
            Camera.Size size = parameters.getPreviewSize();
            byte[] data = new byte[size.width*size.height*
                    ImageFormat.getBitsPerPixel(parameters.getPreviewFormat())/8];
            camera.addCallbackBuffer(data);
        }
        camera.startPreview();
    }


}
*/


public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "Preview";
    private SurfaceHolder mHolder;
    private Camera mCamera;
    
    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here
        Camera.Parameters parameters = mCamera.getParameters();
        Display display = ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        switch(display.getRotation()){
            case Surface.ROTATION_0:
                mCamera.setDisplayOrientation(90);
                break;
            case Surface.ROTATION_270:
                mCamera.setDisplayOrientation(180);
                break;
            case Surface.ROTATION_90:
            case Surface.ROTATION_180:
        }
        mCamera.setParameters(parameters);

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
}
