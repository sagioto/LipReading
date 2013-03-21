package edu.lipreading;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_objdetect;
import edu.lipreading.classification.Classifier;
import edu.lipreading.classification.MultiLayerPerceptronClassifier;
import edu.lipreading.normalization.*;
import edu.lipreading.vision.AbstractFeatureExtractor;
import edu.lipreading.vision.NoMoreStickersFeatureExtractor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

import static com.googlecode.javacv.cpp.opencv_core.cvLoad;

public class LipReadingActivity extends Activity implements TextToSpeech.OnInitListener {
    private TextToSpeech tts;
    private ImageButton recordButton;
    private TextView output;
    private CameraPreview cm;
    private boolean isRecording = false;
    private MouthView mouthView;
    private Classifier classifier;
    private Sample sample;
    private AbstractFeatureExtractor featureExtractor = new NoMoreStickersFeatureExtractor();
    private Normalizer cn = new CenterNormalizer();
    private Normalizer tn = new LinearStretchTimeNormalizer();
    private Normalizer rn = new RotationNormalizer();
    private Normalizer sfn = new SkippedFramesNormalizer();
    private Normalizer rsn = new ResolutionNormalizer();

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

        // Create our Preview view and set it as the content of our activity.
        try{
            mouthView = new MouthView(this);
            cm = new CameraPreview(this, mouthView);
            FrameLayout previewLayout = (FrameLayout) findViewById(R.id.previewLayout);
            previewLayout.addView(cm);
            recordButton = (ImageButton) findViewById(R.id.recordButton);
            output = (TextView) findViewById(R.id.output);
            classifier = new MultiLayerPerceptronClassifier(getAssets().open("yesnohello2.model"));
            initFeatureExtractor();
        } catch (Exception e) {
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
                            sample = new Sample("live");
                        } else {
                            recordButton.setImageResource(R.drawable.record);
                            String ans = classifier.test(LipReading.normelize(sample, sfn, cn, tn));
                            ans = ans.replaceAll(" i ", " I ");
                            ans = ans.replaceAll(" i'", " I'");
                            ans = ans.substring(0,1).toUpperCase() + ans.substring(1, ans.length());
                            output.setText(ans);
                            speakOut();
                        }
                    }
                }
        );
    }

    private void initFeatureExtractor() throws IOException {
        // Load the classifier file from Java resources.
        // cvLoad must get file path so I copy it from assets to external storage and delete it later
        File externalClassifier = getFile("haarcascade_mcs_mouth.xml");

        // Preload the opencv_objdetect module to work around a known bug.
        Loader.load(opencv_objdetect.class);

        opencv_objdetect.CvHaarClassifierCascade classifier = new opencv_objdetect.CvHaarClassifierCascade(cvLoad(externalClassifier.getAbsolutePath()));
        externalClassifier.delete();
        if (classifier.isNull()) {
            throw new IOException("Could not load the classifier file.");
        }
        ((NoMoreStickersFeatureExtractor) featureExtractor).setClassifier(classifier);
        ((NoMoreStickersFeatureExtractor) featureExtractor).setStorage(opencv_core.CvMemStorage.create());
    }

    private File getFile(String fileName) throws IOException {
        String s = Utils.convertStreamToString(this.getAssets().open(fileName));
        File dir = this.getExternalFilesDir(fileName.split("\\.")[1]);
        dir.mkdirs();
        File externalClassifier = new File(dir, fileName);
        FileWriter fw = new FileWriter(externalClassifier);
        fw.write(s);
        fw.close();
        return externalClassifier;
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

    private Sample getSample(){
        return this.sample;
    }

    private AbstractFeatureExtractor getFeatureExtractor(){
        return this.featureExtractor;
    }
}

