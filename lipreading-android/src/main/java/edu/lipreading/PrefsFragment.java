package edu.lipreading;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created with IntelliJ IDEA.
 * User: Sagi
 * Date: 23/03/13
 * Time: 14:32
 */
public class PrefsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        ListPreference vocabularyPref = (ListPreference) findPreference("vocabularyPref");
        String[] entries = Constants.VOCABULARY.toArray(new String[Constants.VOCABULARY.size()]);
        vocabularyPref.setEntries(entries);
        vocabularyPref.setEntryValues(entries);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.xml.preferences, container, false);
    }
}

