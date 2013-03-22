package edu.lipreading;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.AsyncTask;
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
import edu.lipreading.classification.TimeWarperClassifier;
import edu.lipreading.normalization.*;
import edu.lipreading.vision.AbstractFeatureExtractor;
import edu.lipreading.vision.NoMoreStickersFeatureExtractor;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static com.googlecode.javacv.cpp.opencv_core.cvLoad;

public class LipReadingActivity extends Activity implements TextToSpeech.OnInitListener {
    private final String TAG = LipReadingActivity.class.getSimpleName();
    private TextToSpeech tts;
    private ImageButton recordButton;
    private TextView output;
    private CameraPreview cm;
    private boolean isRecording = false;
    private boolean firstTime = true;
    private MouthView mouthView;
    private Classifier classifier;
    private Sample sample = new Sample("android");
    private AbstractFeatureExtractor featureExtractor;
    private Normalizer cn = new CenterNormalizer();
    private Normalizer tn = new LinearStretchTimeNormalizer();
    private Normalizer sfn = new SkippedFramesNormalizer();

    /**
     * Called when the activity is first created.
     * @param savedInstanceState If the activity is being re-initialized after 
     * previously being shut down then this Bundle contains the data it most 
     * recently supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is null.</b>
     */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);


        // Create our Preview view and set it as the content of our activity.
        try{
            getFile("lr.properties");
            String[] vocabularies = getAssets().list("vocabularies");
            for (String vocabulary : vocabularies) {
                getFile("vocabularies/" + vocabulary);
            }
            setContentView(R.layout.main);
            tts = new TextToSpeech(this, this);
            mouthView = new MouthView(this);
            cm = new CameraPreview(this, mouthView);
            FrameLayout previewLayout = (FrameLayout) findViewById(R.id.previewLayout);
            previewLayout.addView(cm);
            output = (TextView) findViewById(R.id.output);
            initFeatureExtractor();
            final AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    initClassifier();
                    return null;
                }
            };
            task.execute(null);
            recordButton = (ImageButton) findViewById(R.id.recordButton);
            recordButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            isRecording = !isRecording;
                            if(isRecording){
                                if(firstTime){
                                    try {
                                        task.get();
                                    } catch (InterruptedException e) {
                                        handleException(e);
                                    } catch (ExecutionException e) {
                                        handleException(e);
                                    }
                                    firstTime = !firstTime;
                                }
                                recordButton.setImageResource(R.drawable.stop);
                                sample = new Sample("live");
                            } else {
                                recordButton.setImageResource(R.drawable.record);
                                String ans = classifier.test(LipReading.normelize(sample, sfn, cn, tn));
                                ans = makePretty(ans);
                                output.setText(ans);
                                speakOut();
                            }
                        }
                    }
            );



        } catch (Exception e) {
            handleException(e);
        }
    }

    private void initClassifier() {
        try{
            String [] modelParts = getAssets().list("model");
            Vector<InputStream> streamList = new Vector<InputStream>();
            for (String part : modelParts) {
                streamList.add(getAssets().open("model/" + part));
            }
            //classifier = new MultiLayerPerceptronClassifier(new SequenceInputStream(streamList.elements()));
        }catch (Exception e){
            e.printStackTrace();
            handleException(e);
        }
        classifier = new TimeWarperClassifier();
    }

    private void handleException(Exception e) {
        new AlertDialog.Builder(getApplicationContext()).setMessage(e.getMessage()).create().show();
    }

    private String makePretty(String ans) {
        ans = ans.replaceAll(" i ", " I ");
        ans = ans.replaceAll(" i'", " I'");
        ans = ans.substring(0,1).toUpperCase() + ans.substring(1, ans.length());
        return ans;
    }

    private void initFeatureExtractor() throws IOException {
        // Load the classifier file from Java resources.
        // cvLoad must get file path so I copy it from assets to external storage and delete it later
        File externalClassifier = getFile("haarcascade_mcs_mouth.xml");

        // Preload the opencv_objdetect module to work around a known bug.
        Loader.load(opencv_objdetect.class);
        featureExtractor = new NoMoreStickersFeatureExtractor();
        featureExtractor.setGui(false);
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

        String[] split = fileName.split("\\.");
        File dir = this.getExternalFilesDir(split[1]);

        dir.mkdirs();
        String[] split1 = fileName.split("/");
        File externalFile = new File(dir, fileName);
        if(split1.length > 1){
            dir = new File(dir.getAbsolutePath() + "/" + fileName.substring(0, fileName.lastIndexOf("/")));
            dir.mkdirs();
            externalFile = new File(dir, split1[1]);
        }
        FileWriter fw = new FileWriter(externalFile);
        fw.write(s);
        fw.close();
        return externalFile;
    }

    private void printArray(String[] list) {
        for (String s : list) {
            Log.i("######", s);
        }
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

    public Sample getSample(){
        return this.sample;
    }

    public AbstractFeatureExtractor getFeatureExtractor(){
        return this.featureExtractor;
    }
}

