package edu.lipreading;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
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
import edu.lipreading.normalization.CenterNormalizer;
import edu.lipreading.normalization.LinearStretchTimeNormalizer;
import edu.lipreading.normalization.Normalizer;
import edu.lipreading.normalization.SkippedFramesNormalizer;
import edu.lipreading.vision.AbstractFeatureExtractor;
import edu.lipreading.vision.ColoredStickersFeatureExtractor;
import edu.lipreading.vision.NoMoreStickersFeatureExtractor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.googlecode.javacv.cpp.opencv_core.cvLoad;

public class LipReadingActivity extends Activity implements TextToSpeech.OnInitListener {
    private final String TAG = LipReadingActivity.class.getSimpleName();
    private TextToSpeech tts;
    private ImageButton recordButton;
    private TextView output;
    private CameraPreview cameraPreview;
    private AtomicBoolean isRecording = new AtomicBoolean(false);
    private boolean firstTime = true;



    private boolean trainingMode = false;
    private MouthView mouthView;
    private Classifier classifier;
    private Sample sample = new Sample("android");
    private AbstractFeatureExtractor featureExtractor;
    private Normalizer cn = new CenterNormalizer();
    private Normalizer tn = new LinearStretchTimeNormalizer();
    private Normalizer sfn = new SkippedFramesNormalizer();
    private SettingsFragment settingsFragment;
    private SharedPreferences preferences;


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
            settingsFragment = new SettingsFragment(this);
            preferences = getPreferences(MODE_PRIVATE);
            tts = new TextToSpeech(this, this);
            String defVoice = getResources().getString(R.string.male);
            String voiceType = preferences.getString("voiceTypePref", defVoice);
            if(!defVoice.equals(voiceType)){
                tts.setLanguage(Locale.US);
            }
            mouthView = new MouthView(this);
            cameraPreview = new CameraPreview(this, mouthView);
            FrameLayout previewLayout = (FrameLayout) findViewById(R.id.previewLayout);
            previewLayout.addView(cameraPreview);
            previewLayout.addView(mouthView);
            output = (TextView) findViewById(R.id.output);
            String defFE = getResources().getString(R.string.noStickers);
            String FEType = preferences.getString("featureExtractorPref", defFE);
            if(defFE.equals(FEType)){
                initNoStickersExtractor();
            }
            else {
                initStickersExtractor();
            }
            final AsyncTask<Void, Void, Void> initClassifierTask = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    String defClassifier = getResources().getString(R.string.dtw);
                    String classifierType = preferences.getString("featureExtractorPref", defClassifier);
                    if(defClassifier.equals(classifierType)){
                        initDTWClassifier();
                    }
                    else{
                        initMLPClassifier();
                    }
                    return null;
                }
            };
            initClassifierTask.execute(null);
            recordButton = (ImageButton) findViewById(R.id.recordButton);
            recordButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            recordButton.setImageResource(R.drawable.stop);
                            sample = new Sample("live");
                            isRecording.set(!isRecording.get());
                            if(isRecording.get()){
                                if(firstTime){
                                    try {
                                        initClassifierTask.get();
                                    } catch (InterruptedException e) {
                                        handleException(e);
                                    } catch (ExecutionException e) {
                                        handleException(e);
                                    }
                                    firstTime = !firstTime;
                                }
                            } else {
                                recordButton.setImageResource(R.drawable.record);
                                String ans = "hello";//classifier.test(LipReading.normelize(sample, sfn, cn, tn));
                                ans = makePretty(ans);
                                output.setText(ans);
                                speakOut();
                                if(trainingMode){
                                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which){
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    sample.setLabel(output.getText().toString());
                                                    //upload sample
                                                    break;

                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(LipReadingActivity.this);
                                                    builder.setTitle(getString(R.string.correctWordQustion));
                                                    final List<String> words = new Vector<String>(Constants.VOCABULARY);
                                                    words.remove(output.getText().toString().toLowerCase());
                                                    for (int i = 0; i < words.size(); i++) {
                                                        words.set(i, makePretty(words.get(i)));
                                                    }
                                                    builder.setItems(words.toArray(new String[words.size()]), new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int item) {
                                                            sample.setLabel(words.get(item).toLowerCase());
                                                            //upload sample
                                                        }
                                                    });
                                                    AlertDialog alert = builder.create();
                                                    alert.show();

                                                    break;
                                            }

                                        }
                                    };

                                    AlertDialog.Builder builder = new AlertDialog.Builder(LipReadingActivity.this);
                                    builder.setMessage(getString(R.string.wasCorrectQuestion)).setPositiveButton(getString(R.string.yes), dialogClickListener)
                                            .setNegativeButton(getString(R.string.no), dialogClickListener).show();
                                }
                            }
                        }
                    }
            );
        } catch (Exception e) {
            handleException(e);
        }
    }

    private void handleException(Exception e) {
        new AlertDialog.Builder(this).setMessage(e.getMessage()).create().show();
    }

    public String makePretty(String ans) {
        ans = ans.replaceAll(" i ", " I ");
        ans = ans.replaceAll(" i'", " I'");
        ans = ans.substring(0,1).toUpperCase() + ans.substring(1, ans.length());
        return ans;
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

    @Override
    public boolean onKeyDown(int keycode, KeyEvent event ) {
        if(keycode == KeyEvent.KEYCODE_MENU){
            launchPrefs();
        }
        return super.onKeyDown(keycode,event);
    }

    private void launchPrefs() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction()
                .replace(android.R.id.content, settingsFragment);
        transaction.addToBackStack( null );
        transaction.commit();
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

    public boolean isRecording() {
        return isRecording.get();
    }

    public TextToSpeech getTts() {
        return tts;
    }

    public Classifier getClassifier() {
        return classifier;
    }

    public boolean isTrainingMode() {
        return trainingMode;
    }
    public void setTrainingMode(boolean trainingMode) {
        this.trainingMode = trainingMode;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        tts.shutdown();
        mouthView.shutdown();
        if(featureExtractor instanceof NoMoreStickersFeatureExtractor)
            ((NoMoreStickersFeatureExtractor)featureExtractor).shutdown();
    }





    protected void initStickersExtractor() {
        if(featureExtractor instanceof ColoredStickersFeatureExtractor){
            return;
        }
        featureExtractor = new ColoredStickersFeatureExtractor();
        featureExtractor.setGui(false);
    }

    protected void initNoStickersExtractor() {
        if(featureExtractor instanceof NoMoreStickersFeatureExtractor){
            return;
        }
        try {
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
        } catch (IOException e) {
            e.printStackTrace();
            handleException(e);
        }

    }

    protected void initDTWClassifier() {
        if(classifier instanceof TimeWarperClassifier){
            return;
        }
        try{

            String fileNameFromUrl = Utils.getFileNameFromUrl(Constants.DEFAULT_TRAINING_SET_ZIP);
            String trainingSetFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    .getAbsolutePath() + "/" + fileNameFromUrl;
            if(!new File(trainingSetFilePath).exists()){
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(Constants.DEFAULT_TRAINING_SET_ZIP));
                request.setDescription("Downloading the Training set for the Lip Reading Classifier");
                request.setTitle("Lip Reading Training Set");
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileNameFromUrl);
                DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                long id = manager.enqueue(request);
                manager.getUriForDownloadedFile(id);
            }
            classifier = new TimeWarperClassifier();
            classifier.train(Utils.getTrainingSetFromZip(
                    trainingSetFilePath));

        }catch (Exception e){
            e.printStackTrace();
            handleException(e);
        }
    }

    protected void initMLPClassifier() {
        if(classifier instanceof MultiLayerPerceptronClassifier){
            return;
        }
        try {
            /*String [] modelParts = getAssets().list("model");
            Vector<InputStream> streamList = new Vector<InputStream>();
            for (String part : modelParts) {
                streamList.add(getAssets().open("model/" + part));
            }
            classifier = new MultiLayerPerceptronClassifier(new SequenceInputStream(streamList.elements()));*/
        } catch (Exception e) {
            e.printStackTrace();
            handleException(e);
        }
    }
}

