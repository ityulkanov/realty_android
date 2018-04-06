package ru.dom_v.BlackList;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

/**
 * Created by user on 20.02.2017.
 */

public class PreferencesActivity extends PreferenceActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        Preference customPref = (Preference) findPreference("mainSettings");
        customPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SharedPreferences customSharedPreference = getSharedPreferences(
                        "myCustomSharedPrefs", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = customSharedPreference
                        .edit();
                editor.putString("myCustomPref",
                        "The preference has been clicked");
                editor.commit();
                return true;
            }
        });

    }
}
