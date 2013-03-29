package edu.lipreading;

import android.app.*;
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
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import edu.lipreading.classification.Classifier;
import edu.lipreading.classification.MultiLayerPerceptronClassifier;
import edu.lipreading.classification.TimeWarperClassifier;
import edu.lipreading.vision.AbstractFeatureExtractor;
import edu.lipreading.vision.ColoredStickersFeatureExtractor;
import edu.lipreading.vision.NoMoreStickersFeatureExtractor;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.googlecode.javacv.cpp.opencv_core.cvLoad;

public class LipReadingActivity extends Activity implements TextToSpeech.OnInitListener {
    private final String TAG = Thread.currentThread().getStackTrace()[1].getClassName();
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
    private SettingsFragment settingsFragment;
    private SharedPreferences preferences;
    private Client client = Client.create();
    private WebResource resource = client.resource(Constants.SERVER_URL);
    private int sentSampleId = -1;


    /**
     * Called when the activity is first created.
     * @param savedInstanceState If the activity is being re-initialized after 
     * previously being shut down then this Bundle contains the data it most 
     * recently supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is null.</b>
     */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        try{
            preferences = getPreferences(MODE_PRIVATE);
            setTrainingMode(preferences.getBoolean("trainingModePref", false));
            setContentView(R.layout.main);
            settingsFragment = new SettingsFragment(this);
            tts = new TextToSpeech(this, this);
            mouthView = new MouthView(this);
            cameraPreview = new CameraPreview(this, mouthView);
            FrameLayout previewLayout = (FrameLayout) findViewById(R.id.previewLayout);
            previewLayout.addView(cameraPreview);
            previewLayout.addView(mouthView);
            output = (TextView) findViewById(R.id.output);
            final AsyncTask<Boolean, Void, Void> initFeatureExtractorTask = asyncInitFeatureExtractor();
            recordButton = (ImageButton) findViewById(R.id.recordButton);
            recordButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onRecordButtonPressed(initFeatureExtractorTask);
                        }
                    }
            );
        } catch (Exception e) {
            handleException(e);
        }
    }

    private void onRecordButtonPressed(AsyncTask<Boolean, Void, Void> initFeatureExtractorTask) {
        recordButton.setImageResource(R.drawable.stop);
        isRecording.set(!isRecording.get());
        if(isRecording.get()){
            sample = new Sample("live");
        } else {
            onStopButtonPressed(initFeatureExtractorTask);
        }
    }

    private void onStopButtonPressed(AsyncTask<Boolean, Void, Void> initFeatureExtractorTask) {
        recordButton.setImageResource(R.drawable.record);
        if(firstTime){
            onFirstTimeStopButtonPressed(initFeatureExtractorTask);
        }
        SamplePacket toSend = Utils.getPacketFromSample(sample);
        String response = "";
        try{
        response = resource.path("/lipreading/samples")
                .type(MediaType.APPLICATION_JSON_TYPE)
                .header("training", isTrainingMode())
                .post(String.class, toSend);
        }  catch (UniformInterfaceException ue) {
            handleRequstError(ue);
        }
        if("".equals(response)){
            showErrorMessage("Didn't get classification from server");
        }
        String[] split = response.split(",");
        String ans = split[0];
        sentSampleId = Integer.parseInt(split[1]);
        ans = makePretty(ans);
        output.setText(ans);
        speakOut();
        if(trainingMode){
            onStopButtonPressedInTrainingMode();
        }
    }

    private void handleRequstError(UniformInterfaceException ue) {
        ClientResponse clientResponse = ue.getResponse();
        String entity = clientResponse.getEntity(String.class);
        showErrorMessage("got status " + clientResponse.getStatus() + "\nand entity: " + entity);
    }

    private void onStopButtonPressedInTrainingMode() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        try{
                            putLabel(output.getText().toString().toLowerCase());
                        }  catch (UniformInterfaceException ue) {
                            handleRequstError(ue);
                        }
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        onWrongRecognitionButtonPressed();
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(LipReadingActivity.this);
        builder.setMessage(getString(R.string.wasCorrectQuestion)).setPositiveButton(getString(R.string.yes), dialogClickListener)
                .setNegativeButton(getString(R.string.no), dialogClickListener).show();
    }

    private void putLabel(String label) {
        String response = resource.path("/lipreading/samples")
                .type(MediaType.APPLICATION_JSON_TYPE)
                .header("id", sentSampleId)
                .put(String.class, label);
        if(!"OK".equals(response))
            showErrorMessage("Something went wrong with training");
    }

    private void onWrongRecognitionButtonPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.correctWordQustion));
        final List<String> words = new Vector<String>(Constants.VOCABULARY);
        words.remove(output.getText().toString().toLowerCase());
        for (int i = 0; i < words.size(); i++) {
            words.set(i, makePretty(words.get(i)));
        }
        builder.setItems(words.toArray(new String[words.size()]), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                putLabel(words.get(item).toLowerCase());
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void onFirstTimeStopButtonPressed(AsyncTask<Boolean, Void, Void> initFeatureExtractorTask) {
        try {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.loadingClassifier));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            runOnUiThread(new Runnable() {
                public void run() {
                    progressDialog.show();
                }
            });
            initFeatureExtractorTask.get();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                }
            });

        } catch (Exception e) {
            handleException(e);
        }
        firstTime = !firstTime;
    }

    private AsyncTask<Boolean, Void, Void> asyncInitFeatureExtractor() {
        final AsyncTask<Boolean, Void, Void> initClassifierTask = new AsyncTask<Boolean, Void, Void>() {
            @Override
            protected Void doInBackground(Boolean... params) {
                initFeatureExtractor();
                String defClassifier = getResources().getString(R.string.dtw);
                String classifierType = preferences.getString("classifierPref", defClassifier);
                if(defClassifier.equals(classifierType)){
                    initDTWClassifier();
                }
                else{
                    initMLPClassifier();
                }

                return null;
            }
        };
        initClassifierTask.execute(true);
        return initClassifierTask;
    }

    private void initFeatureExtractor() {
        String defFE = getResources().getString(R.string.stickers);
        String FEType = preferences.getString("featureExtractorPref", defFE);
        if(defFE.equals(FEType)){
            initNoStickersExtractor();
        }
        else {
            initStickersExtractor();
        }
    }

    private void handleException(Exception e) {
        showErrorMessage(e.getMessage());
    }
    private void showErrorMessage(String message) {
        new AlertDialog.Builder(this).setMessage(message).create().show();
    }

    public String makePretty(String ans) {
        ans = ans.replaceAll(" i ", " I ");
        ans = ans.replaceAll(" i'", " I'");
        ans = ans.substring(0,1).toUpperCase(Locale.US) + ans.substring(1, ans.length());
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
            int result;
            String defVoice = getResources().getString(R.string.male);
            String voiceType = preferences.getString("voiceTypePref", defVoice);
            if(!defVoice.equals(voiceType)){
                result = tts.setLanguage(Locale.US);
            } else {
                result = tts.setLanguage(Locale.UK);
            }
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
            launchSettings();
        }
        return super.onKeyDown(keycode,event);
    }

    private void launchSettings() {
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
        } catch (Exception e) {
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
            classifier.train(Utils.getTrainingSetFromZip(trainingSetFilePath));

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

