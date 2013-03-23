package edu.lipreading;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: Sagi
 * Date: 23/03/13
 * Time: 14:32
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{
    private LipReadingActivity context;

    public SettingsFragment(LipReadingActivity context){
        super();
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        setPreferenceScreen((PreferenceScreen) findPreference("screen"));
        ListPreference vocabularyPref = (ListPreference) findPreference("vocabularyPref");
        String[] entries = Constants.VOCABULARY.toArray(new String[Constants.VOCABULARY.size()]);
        vocabularyPref.setEntries(entries);
        vocabularyPref.setEntryValues(entries);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.setBackground(new ColorDrawable(Color.BLACK));
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

        if("trainingModePref".equals(key))
            context.setTrainingMode(sharedPreferences.getBoolean("trainingModePref", false));
        else if("voiceTypePref".equals(key)){
            String defaultVoice = getResources().getString(R.string.male);
            String voiceTypePref = sharedPreferences.getString("voiceTypePref", defaultVoice);
            if(defaultVoice.equals(voiceTypePref)){
                this.context.getTts().setLanguage(Locale.UK);
            } else {
                this.context.getTts().setLanguage(Locale.US);
            }
            preferences.edit().putString(key, voiceTypePref).commit();
        } else if("classifierPref".equals(key)){
            String defaultClassifier = getResources().getString(R.string.mlp);
            String classifierType = sharedPreferences.getString(key, defaultClassifier);
            if(defaultClassifier.equals(classifierType)){
                this.context.initMLPClassifier();
            } else {
                this.context.initDTWClassifier();
            }
            preferences.edit().putString(key, classifierType).commit();
        } else if("featureExtractorPref".equals(key)){
            String defaultFeatureExtractor = getResources().getString(R.string.noStickers);
            String feType = sharedPreferences.getString(key, defaultFeatureExtractor);
            if(defaultFeatureExtractor.equals(feType)){
                this.context.initNoStickersExtractor();
            } else {
                this.context.initStickersExtractor();
            }
            preferences.edit().putString(key, feType).commit();
        }

    }
}
