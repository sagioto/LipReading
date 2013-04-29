package edu.lipreading.android;

import java.util.List;
import java.util.Locale;
import java.util.Vector;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import edu.lipreading.Constants;

/*
 * Created with IntelliJ IDEA.
 * User: Sagi
 * Date: 23/03/13
 * Time: 14:32
 */

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{
	private LipReadingActivity context;

	public SettingsFragment(){
		super();        
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);
		setPreferenceScreen((PreferenceScreen) findPreference("screen"));
		ListPreference vocabularyPref = (ListPreference) findPreference("vocabularyPref");
		List<String> words = new Vector<String>(Constants.VOCABULARY);
		for (int i = 0; i < words.size(); i++) {
			words.set(i, context.makePretty(words.get(i)));
		}
		String[] entries = words.toArray(new String[words.size()]);
		vocabularyPref.setEntries(entries);
		vocabularyPref.setEntryValues(entries);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);
		view.setBackgroundColor(Color.BLACK);
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

	}

	@Override
	public void onPause() {
		getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}


	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		SharedPreferences preferences = context.getPreferences(Context.MODE_PRIVATE);

		if(key.equals(getString(R.string.trainingModePref))){
			boolean trainingModePref = sharedPreferences.getBoolean(getString(R.string.trainingModePref), false);
			context.setTrainingMode(trainingModePref);
			preferences.edit().putBoolean(key, trainingModePref).apply();
		} else if(key.equals(getString(R.string.voiceTypePref))){
			String defaultVoice = getResources().getString(R.string.male);
			String voiceTypePref = sharedPreferences.getString(getString(R.string.voiceTypePref), defaultVoice);
			if(defaultVoice.equals(voiceTypePref)){
				this.context.getTts().setLanguage(Locale.UK);
			} else {
				this.context.getTts().setLanguage(Locale.US);
			}
			preferences.edit().putString(key, voiceTypePref).apply();
		} else if(key.equals(getString(R.string.serverPref))){
			String currentUrl = sharedPreferences.getString(getString(R.string.serverPref), getString(R.string.serverDef));
			this.context.setUri(currentUrl);
			preferences.edit().putString(key, currentUrl).apply();
		}
	}

	public SettingsFragment setContext(LipReadingActivity context){
		this.context = context;
		return this;
	}
}
