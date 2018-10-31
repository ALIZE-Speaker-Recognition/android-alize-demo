package fr.univavignon.alize.AndroidALIZEDemo;

import android.app.AlertDialog;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;

import AlizeSpkRec.AlizeException;

public class SettingsActivity extends PreferenceActivity {

    private EditText thresholdEditText;
    private AlertDialog thresholdDialog;
    private DemoSpkRecSystem demoSpkRecSystem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        try {
            demoSpkRecSystem = DemoSpkRecSystem.getSharedInstance(SettingsActivity.this);

        } catch (AlizeException | IOException e) {
            e.printStackTrace();
        }

        Preference resetSpeakersList = findPreference("reset_speakers_list");
        if (resetSpeakersList != null) {
            resetSpeakersList.setOnPreferenceClickListener(preferenceListListener);
        }

        Preference threshold = findPreference("threshold");
        if (threshold != null) {
            threshold.setOnPreferenceClickListener(preferenceListListener);
        }

        Preference saveModels = findPreference("save_speakers_models");
        if (saveModels != null) {
            saveModels.setOnPreferenceClickListener(preferenceListListener);
        }
    }

    public Preference.OnPreferenceClickListener preferenceListListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            try {
                switch (preference.getKey()) {
                    case "threshold":
                        EditTextPreference threshold = (EditTextPreference) preference;
                        thresholdDialog = (AlertDialog) threshold.getDialog();

                        thresholdEditText = threshold.getEditText();
                        thresholdEditText.addTextChangedListener(thresholdListener);
                        break;
                    case "reset_speakers_list":
                        //Load the saved models
                        demoSpkRecSystem.loadSavedModels();

                        finish();
                        break;
                    case "save_speakers_models":
                        SwitchPreference saveModels = (SwitchPreference) preference;
                        if (saveModels.isChecked()) {
                            demoSpkRecSystem.syncToDisc();
                        }
                        break;
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
}
