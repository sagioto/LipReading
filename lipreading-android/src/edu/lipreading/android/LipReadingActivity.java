package edu.lipreading.android;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.gson.Gson;

import edu.lipreading.Constants;
import edu.lipreading.Sample;
import edu.lipreading.SamplePacket;

public class LipReadingActivity extends Activity implements CvCameraViewListener2, TextToSpeech.OnInitListener{


	private static final String TAG = "LipReadingActivity";
	private String uri = "192.168.0.101:9998"; /*Constants.SERVER_URL*/
	private Mat rgba;
	private Mat gray;
	private File cascadeFile;
	private FeatureExtractor fe;
	private CameraBridgeViewBase openCvCameraView;
	private Sample sample;
	private TextToSpeech tts;
	private ImageButton recordButton;
	private TextView output;
	private boolean isRecording = false;
	private boolean isTrainingMode = false;
	private HttpClient client;
	private int sentSampleId = -1;
	private Gson gson = new Gson();
	private SettingsFragment settingsFragment;
	private SharedPreferences preferences;
	private Object monitor = new Object();
	private BaseLoaderCallback  loaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS:
			{
				Log.i(TAG, "OpenCV loaded successfully");

				// Load native library after(!) OpenCV initialization
				System.loadLibrary("feature_extractor");

				try {
					// load cascade file from application resources
					InputStream is = getResources().openRawResource(R.raw.haarcascade_mcs_mouth);
					File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
					cascadeFile = new File(cascadeDir, "haarcascade_mcs_mouth.xml");
					FileOutputStream os = new FileOutputStream(cascadeFile);
					byte[] buffer = new byte[4096];
					int bytesRead;
					while ((bytesRead = is.read(buffer)) != -1) {
						os.write(buffer, 0, bytesRead);
					}
					is.close();
					os.close();

					//openCvCameraView.setMaxFrameSize(800, 800);
					fe = new FeatureExtractor(cascadeFile.getAbsolutePath(), openCvCameraView.getHeight(), openCvCameraView.getWidth());
					cascadeFile.delete();
				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
				}

				openCvCameraView.enableView();
			} break;
			default:
			{
				super.onManagerConnected(status);
			} break;
			}
		}
	};

	public LipReadingActivity() {
		Log.i(TAG, "Instantiated new " + this.getClass());
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.main);
		openCvCameraView = (CameraBridgeViewBase) findViewById(R.id.lip_reading_surface_view);
		openCvCameraView.setCvCameraViewListener(this);
		client = AndroidHttpClient.newInstance("lipreading-android");
		tts = new TextToSpeech(this, this);
		preferences = getPreferences(MODE_PRIVATE);
		isTrainingMode = preferences.getBoolean(getString(R.string.trainingModePref), false);
		uri = preferences.getString(getString(R.string.serverPref), getString(R.string.serverDef));
		settingsFragment = new SettingsFragment().setContext(this);
		output = (TextView) findViewById(R.id.output);
		recordButton = (ImageButton) findViewById(R.id.recordButton);		
		recordButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onRecordButtonPressed();
					}
				});
	}

	private void onRecordButtonPressed() {
		recordButton.setImageResource(R.drawable.stop);
		isRecording = !isRecording;
		if(isRecording){
			sample = new Sample("android" + System.currentTimeMillis());
		} else {
			onStopButtonPressed();
		}
	}

	private void onStopButtonPressed() {
		recordButton.setImageResource(R.drawable.record);
		new Thread(new Runnable(){

			@Override
			public void run() {
				SamplePacket toSend = getPacketFromSample(sample);
				toSend.setHeight(rgba.height());
				toSend.setWidth(rgba.width());
				toSend.setOriginalMatrixSize(sample.getMatrix().size());
				String response = "";
				try{
					HttpPost post = new HttpPost("http://" + uri + "/samples");
					post.setHeader("training", "" + isTrainingMode);
					String json = gson.toJson(toSend);
					json = hanldeWeirdJson(json, toSend.getMatrix());
					StringEntity entity = new StringEntity(json, HTTP.UTF_8);
					entity.setContentType("application/json");
					post.setEntity(entity);
					HttpResponse httpResponse = client.execute(post);
					response = convertStreamToString(httpResponse.getEntity().getContent());
				}  catch (final Exception e) {
					runOnUiThread(new Runnable(){ @Override public void run() {handleExcpetion(e);}});
				}
				if("".equals(response)){
					final String msg = response;
					runOnUiThread(new Runnable(){ @Override public void run() {showErrorMessage("Didn't get classification from server. got: " + msg);}});
				} else {
					String[] split = response.split(",");
					String ans = split[0];
					sentSampleId = Integer.parseInt(split[1]);
					final String answer = makePretty(ans);
					runOnUiThread(new Runnable() { @Override public void run() {
						output.setText(answer);
						speakOut();
						if(isTrainingMode)
							onStopButtonPressedInTrainingMode();
					}});			
				}
			}

			private String hanldeWeirdJson(String json, int [][]matrix) {
				String ans = "{\"matrix\":[";
				for (int i = 0; i < matrix.length; i++) {
					ans += "{\"item\":[";
					for (int j = 0; j < matrix[i].length; j++) {
						ans += "\"" + matrix[i][j] + "\"";
						if(j != matrix[i].length - 1){
							ans += ",";
						}
					}
					ans += "]}";
					if(i != matrix.length - 1){
						ans += ",";
					}
				}
				json = json.substring(json.indexOf("],\"id"), json.length());			
				return ans + json;	
			}

		}).start();
	}

	private void onStopButtonPressedInTrainingMode() {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
				case DialogInterface.BUTTON_POSITIVE:
					try{
						putLabel(output.getText().toString().toLowerCase(Locale.US));
					}  catch (Exception e) {
						handleExcpetion(e);
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

	private void putLabel(final String label) {
		new Thread(new Runnable(){
			@Override
			public void run() {
				HttpPut put = new HttpPut("http://" + uri + "/samples/" + sentSampleId);
				StringEntity entity;
				try {
					entity = new StringEntity(label, HTTP.UTF_8);
					entity.setContentType("application/json");
					put.setEntity(entity);
					HttpResponse httpResponse = client.execute(put);
					String response = convertStreamToString(httpResponse.getEntity().getContent());
					if(!"OK".equals(response))
						runOnUiThread(new Runnable(){ @Override public void run() {showErrorMessage("Something went wrong with training");}});
				} catch (final Exception e) {
					runOnUiThread(new Runnable(){ @Override public void run() {handleExcpetion(e); }});
				}
			}}).start();

	}

	private void onWrongRecognitionButtonPressed() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.correctWordQustion));
		final List<String> words = new Vector<String>(Constants.VOCABULARY);
		words.remove(output.getText().toString().toLowerCase(Locale.US));
		for (int i = 0; i < words.size(); i++) {
			words.set(i, makePretty(words.get(i)));
		}
		builder.setItems(words.toArray(new String[words.size()]), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				putLabel(words.get(item).toLowerCase(Locale.US));
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	/*private void handleRequestError(UniformInterfaceException ue) {
		ClientResponse clientResponse = ue.getResponse();
		String entity = clientResponse.getEntity(String.class);
		showErrorMessage("got status " + clientResponse.getStatus() + "\nand entity: " + entity);
	}*/

	private void handleExcpetion(Exception e) {
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

	private void speakOut() {
		String text = output.getText().toString();
		tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
	}

	@Override
	public void onPause()
	{
		super.onPause();
		if(fe != null){
			synchronized (monitor) {
				fe.release();	
			}
		}			
		if (openCvCameraView != null)
			openCvCameraView.disableView();
	}

	@Override
	public void onResume()
	{
		super.onResume();	
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_4, this, loaderCallback);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		openCvCameraView.disableView();
		tts.shutdown();
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		gray = new Mat();
		rgba = new Mat();
	}

	@Override
	public void onCameraViewStopped() {
		gray.release();
		rgba.release();
	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

		rgba = inputFrame.rgba();
		gray = inputFrame.gray();


		int[] points = new int[12];
		synchronized (monitor) {
			fe.detect(gray, rgba, points);
		}
		if(isRecording){
			List<Integer> coordinats = new Vector<Integer>();
			for (int i = 0; i < 8; i++) {
				coordinats.add(points[i]);
			}
			sample.getMatrix().add(coordinats);
		}


		return rgba;
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

	public static Sample getSampleFromPacket(SamplePacket sp) {
		Sample sample = new Sample();
		sample.setHeight(sp.getHeight());
		sample.setWidth(sp.getWidth());
		sample.setOriginalMatrixSize(sp.getOriginalMatrixSize());
		sample.setId(sp.getId());
		sample.setLabel(sp.getLabel());

		for(int i=0; i<sp.getMatrix().length; i++) {
			Vector<Integer> vec = new Vector<Integer>();
			for(int j=0; j<sp.getMatrix()[i].length; j++) {
				vec.add(sp.getMatrix()[i][j]);
			}
			sample.getMatrix().add(vec);
		}

		return sample;
	}

	public static SamplePacket getPacketFromSample(Sample s) {
		SamplePacket sample = new SamplePacket();
		sample.setHeight(s.getHeight());
		sample.setWidth(s.getWidth());
		sample.setLabel(s.getLabel());
		sample.setId(s.getId());
		sample.setOriginalMatrixSize(s.getOriginalMatrixSize());

		int[][] matrix = new int[s.getMatrix().size()][];
		for(int i=0; i<matrix.length; i++) {
			matrix[i] = new int[s.getMatrix().get(i).size()];
			for(int j=0; j<matrix[i].length; j++) {
				matrix[i][j] = s.getMatrix().get(i).get(j);
			}
		}
		sample.setMatrix(matrix);
		return sample;
	}

	public static String convertStreamToString(InputStream is) {
		String ans = "";
		Scanner s = new Scanner(is);
		s.useDelimiter("\\A");
		ans = s.hasNext() ? s.next() : "";
		s.close();
		return ans;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	public void launchSettings(MenuItem item) {
		FragmentTransaction transaction = getFragmentManager().beginTransaction()
				.replace(android.R.id.content, settingsFragment);
		transaction.addToBackStack( null );
		transaction.commit();
	}

	public void setTrainingMode(boolean trainingModePref) {
		this.isTrainingMode = trainingModePref;
	}

	public TextToSpeech getTts() {
		return this.tts;
	}

	public void setUri(String url){
		this.uri = url;
	}
}
