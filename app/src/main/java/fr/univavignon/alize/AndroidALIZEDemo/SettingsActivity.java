package fr.univavignon.alize.AndroidALIZEDemo;

import android.app.AlertDialog;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;

import AlizeSpkRec.AlizeException;
import AlizeSpkRec.SimpleSpkDetSystem;

public class SettingsActivity extends PreferenceActivity {

    private EditText thresholdEditText;
    private AlertDialog thresholdDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        Preference resetSpeakersList = findPreference("reset_speakers_list");
        if (resetSpeakersList != null) {
            resetSpeakersList.setOnPreferenceClickListener(preferenceListListener);
        }

        Preference threshold = findPreference("threshold");
        if (threshold != null) {
            threshold.setOnPreferenceClickListener(preferenceListListener);
        }
    }

    public Preference.OnPreferenceClickListener preferenceListListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            try {
                if (preference.getKey().equals("threshold")) {
                    EditTextPreference threshold = (EditTextPreference)preference;
                    thresholdDialog = (AlertDialog)threshold.getDialog();

                    thresholdEditText = threshold.getEditText();
                    thresholdEditText.addTextChangedListener(thresholdListener);
                }
                else if (preference.getKey().equals("reset_speakers_list")) {
                    resetSpeakersList();
                }
            }
            catch (AlizeException | IOException e) {
                e.printStackTrace();
            }
            return true;
        }
    };

    private TextWatcher thresholdListener = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String threshold = charSequence.toString();
            Button okButton = thresholdDialog.getButton(AlertDialog.BUTTON_POSITIVE);

            try {
                Integer.parseInt(threshold);
                okButton.setEnabled(true);
                thresholdEditText.setError(null);
            }
            catch (NumberFormatException e) {
                okButton.setEnabled(false);
                thresholdEditText.setError(getString(R.string.invalid_threshold));
            }
        }

        @Override
        public void afterTextChanged(Editable editable) { }
    };

    public void resetSpeakersList() throws IOException, AlizeException {
        SimpleSpkDetSystem alizeSystem = SharedAlize.getInstance(SettingsActivity.this);
    }
}
