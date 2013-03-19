package edu.lipreading;

import android.app.Activity;
import android.app.AlertDialog;
import android.hardware.Camera;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.IOException;
import java.util.Locale;

public class LipReadingActivity extends Activity implements TextToSpeech.OnInitListener {
    private TextToSpeech tts;
    private ImageButton recordButton;
    private TextView output;
    private CameraPreview cm;
    private boolean isRecording = false;
/*    private AbstractFeatureExtractor fe = new NoMoreStickersFeatureExtractor();
    private Normalizer cn = new CenterNormalizer();
    private Normalizer tn = new LinearStretchTimeNormalizer();
    private Normalizer rn = new RotationNormalizer();
    private Normalizer rsn = new ResolutionNormalizer();*/



    /**
     * Called when the activity is first created.
     * @param savedInstanceState If the activity is being re-initialized after 
     * previously being shut down then this Bundle contains the data it most 
     * recently supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is null.</b>
     */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.i("started", "onCreate");
        setContentView(R.layout.main);
        tts = new TextToSpeech(this, this);
        Camera camera = Camera.open(getFrontFacingCamera());

        // Create our Preview view and set it as the content of our activity.
        cm = new CameraPreview(this, camera);
        FrameLayout previewLayout = (FrameLayout) findViewById(R.id.previewLayout);
        previewLayout.addView(cm);
        recordButton = (ImageButton) findViewById(R.id.recordButton);
        output = (TextView) findViewById(R.id.output);
        try{
            initFeatureExtractor();
        } catch (IOException e) {
            e.printStackTrace();
            new AlertDialog.Builder(this).setMessage(e.getMessage()).create().show();
        }
        recordButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isRecording = !isRecording;
                        if(isRecording){
                            recordButton.setImageResource(R.drawable.stop);

                        } else {
                            recordButton.setImageResource(R.drawable.record);
          //                  output.setText(classifier.test(sample));
                            speakOut();
                        }
                    }
                }
        );
    }

    private void initFeatureExtractor() throws IOException {
        // Load the classifier file from Java resources.
        // cvLoad must get file path so I copy it from assets to external storage and delete it later
        /*String s = Utils.convertStreamToString(this.getAssets().open("haarcascade_mcs_mouth.xml"));
        File dir = this.getExternalFilesDir("xml");
        dir.mkdirs();
        File externalClassifier = new File(dir, "haarcascade_mcs_mouth.xml");
        FileWriter fw = new FileWriter(externalClassifier);
        fw.write(s);
        fw.close();

        // Preload the opencv_objdetect module to work around a known bug.
        Loader.load(opencv_objdetect.class);

        opencv_objdetect.CvHaarClassifierCascade classifier = new opencv_objdetect.CvHaarClassifierCascade(cvLoad(externalClassifier.getAbsolutePath()));
        externalClassifier.delete();
        if (classifier.isNull()) {
            throw new IOException("Could not load the classifier file.");
        }
        ((NoMoreStickersFeatureExtractor)fe).setClassifier(classifier);
        ((NoMoreStickersFeatureExtractor)fe).setStorage(opencv_core.CvMemStorage.create());*/
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.UK);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                recordButton.setEnabled(true);
            }
        } else {
            Log.e("TTS", "Initialization Failed!");
        }
    }

    private void speakOut() {
        String text = output.getText().toString();
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    public int getFrontFacingCamera() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, info);
            if(info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
                return i;
            }
        }
        throw new IllegalStateException("No front facing camera");
    }
}

