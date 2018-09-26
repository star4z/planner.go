package go.planner.plannergo;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.RingtonePreference;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Set;


/**
 * Handles implementations of settings
 * Created by bdphi on 3/12/2018.
 */

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {
    private static String TAG = "SettingsFragment";
    private Callback callback;

    private static final String KEY_NOTIFY = "NOTIFICATIONS_KEY";

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof Callback) {
            callback = (Callback) activity;
        } else {
            throw new IllegalStateException("Callback interface must be implemented");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        initSummary();

        Preference preference = findPreference(KEY_NOTIFY);
        preference.setOnPreferenceClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

        changeTextColors();

        initSummary();
    }

    @Override
    public void onPause() {
        super.onPause();

        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        // here you should use the same keys as you used in the xml-file
        if (preference.getKey().equals(KEY_NOTIFY)) {
            callback.onNestedPreferenceSelected(NestedPreferencesFragment.KEY_NOTIFY);
        }

        return false;
    }

    private void changeTextColors() {
        if (getActivity() instanceof ColorSchemeActivity) {
            changeTextColors(getPreferenceScreen(), (ColorSchemeActivity) getActivity());
        }
    }

    private void changeTextColors(PreferenceGroup group, ColorSchemeActivity activity) {
        for (int i = 0; i < group.getPreferenceCount(); i++) {
            Preference preference = group.getPreference(i);

            if (preference instanceof PreferenceGroup)
                changeTextColors((PreferenceGroup) preference, activity);
            else {
                if (activity.getColorScheme().equals(ColorScheme.SCHEME_LIGHT))
                    preference.setLayoutResource(R.layout.preference);
                else
                    preference.setLayoutResource(R.layout.preference_dark);

            }
        }
    }


    /**
     * Update summary
     *
     * @param sharedPreferences settings file
     * @param pref              preference to update the summary of
     */
    protected void updatePrefsSummary(SharedPreferences sharedPreferences,
                                      Preference pref) {

        if (pref == null)
            return;

        if (pref instanceof ListPreference) {
            // List Preference
            ListPreference listPref = (ListPreference) pref;
            Log.v("SettingsFragment", "listPrefEntry=" + listPref.getEntry());
            if (listPref.getEntry() == null) {
                listPref.setValueIndex(0);
                Log.v("SettingsFragment", "after change, listPrefEntry=" + listPref.getEntry());
            }
            listPref.setSummary(listPref.getEntry());

        } else if (pref instanceof EditTextPreference) {
            // EditPreference
            EditTextPreference editTextPref = (EditTextPreference) pref;
            editTextPref.setSummary(editTextPref.getText());

        } else if (pref instanceof MultiSelectListPreference) {
            // MultiSelectList Preference
            MultiSelectListPreference mlistPref = (MultiSelectListPreference) pref;
            StringBuilder summaryMListPref = new StringBuilder();
            String and = "";

            // Retrieve values
            Set<String> values = mlistPref.getValues();
            for (String value : values) {
                // For each value retrieve index
                int index = mlistPref.findIndexOfValue(value);
                // Retrieve entry from index
                CharSequence mEntry = index >= 0
                        && mlistPref.getEntries() != null ? mlistPref
                        .getEntries()[index] : null;
                if (mEntry != null) {
                    // add summary
                    summaryMListPref.append(and).append(mEntry);
                    and = ";";
                }
            }
            // set summary
            mlistPref.setSummary(summaryMListPref.toString());

        } else if (pref instanceof RingtonePreference) {
            // RingtonePreference
            RingtonePreference rtPref = (RingtonePreference) pref;
            String uri;
            uri = sharedPreferences.getString(rtPref.getKey(), null);
            if (uri != null) {
                Ringtone ringtone = RingtoneManager.getRingtone(
                        getActivity(), Uri.parse(uri));
                pref.setSummary(ringtone.getTitle(getActivity()));
            }

        } else if (pref instanceof NumberPreference) {
            // My NumberPicker Preference
            NumberPreference nPickerPref = (NumberPreference) pref;
            nPickerPref.setSummary(nPickerPref.getValue());
        }
    }


    protected void initSummary() {
        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
            initPrefsSummary(getPreferenceManager().getSharedPreferences(),
                    getPreferenceScreen().getPreference(i));
        }
    }

    /*
     * Init single Preference
     */
    protected void initPrefsSummary(SharedPreferences sharedPreferences,
                                    Preference p) {
        if (p instanceof PreferenceCategory) {
            PreferenceCategory pCat = (PreferenceCategory) p;
            for (int i = 0; i < pCat.getPreferenceCount(); i++) {
                initPrefsSummary(sharedPreferences, pCat.getPreference(i));
            }
        } else {
            updatePrefsSummary(sharedPreferences, p);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Settings.darkMode))
            getActivity().recreate();
        updatePrefsSummary(sharedPreferences, findPreference(key));
    }

    public interface Callback {
        void onNestedPreferenceSelected(int key);
    }

}
