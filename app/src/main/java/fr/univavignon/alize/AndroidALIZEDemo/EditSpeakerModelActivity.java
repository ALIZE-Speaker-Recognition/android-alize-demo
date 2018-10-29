package fr.univavignon.alize.AndroidALIZEDemo;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Map;

import AlizeSpkRec.AlizeException;
import AlizeSpkRec.IdAlreadyExistsException;

/**
 * Activity use to create and edit a speaker model.
 *
 * @author Nicolas Duret
 */
public class EditSpeakerModelActivity extends RecordActivity {

    private String[] speakers;
    private Button updateSpeaker;
    private EditText editSpeakerName;
    private boolean newSpeaker = false;
    private boolean speakerIdAlreadyExists = false;
    private String currentSpeakerName, originalSpeakerName;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_speaker_model);

        try {
            speakers = alizeSystem.speakerIDs(); //Get all speakers name.
        }
        catch (AlizeException e) {
            e.printStackTrace();
        }

        currentSpeakerName = "";
        String speakerName = getIntent().getStringExtra("speakerName");
        editSpeakerName = findViewById(R.id.add_speaker_name_editText);
        timeText = findViewById(R.id.timeText);
        startRecordButton = findViewById(R.id.startBtn);
        stopRecordButton = findViewById(R.id.stopBtn);
        updateSpeaker = findViewById(R.id.update_speaker_button);

        updateSpeaker.setEnabled(false);
        originalSpeakerName = speakerName;

        if (speakerName.isEmpty()) {
            newSpeaker = true;
            startRecordButton.setVisibility(View.INVISIBLE);
            timeText.setVisibility(View.INVISIBLE);
        }
        else {
            currentSpeakerName = speakerName;
            editSpeakerName.setText(currentSpeakerName);
        }

        String title = "Edit '" + speakerName + "' Model";
        if (newSpeaker) {
            title = getResources().getString(R.string.new_speaker);
            updateSpeaker.setText(R.string.new_speaker);
        }
        setTitle(title);

        editSpeakerName.addTextChangedListener(editSpeakerNameListener);
        updateSpeaker.setOnClickListener(updateSpeakerListener);

        startRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRecording();
            }
        });

        stopRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopRecording();
            }
        });
    }

    private TextWatcher editSpeakerNameListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            currentSpeakerName = charSequence.toString();

            if (speakers.length != 0) {
                speakerIdAlreadyExists = false;

                for (String spkId : speakers) {
                    if (getSpeakerName(spkId).equals(currentSpeakerName) && !currentSpeakerName.equals(originalSpeakerName)) {
                        speakerIdAlreadyExists = true;
                        break;
                    }
                }
                if (speakerIdAlreadyExists) {
                    editSpeakerName.setError(getResources().getString(R.string.speakerExist));
                    updateSpeaker.setEnabled(false);
                }
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (!currentSpeakerName.isEmpty()) {
                startRecordButton.setVisibility(View.VISIBLE);
                timeText.setVisibility(View.VISIBLE);

                if (newSpeaker) {
                    if (recordExists && !speakerIdAlreadyExists) {
                        updateSpeaker.setEnabled(true);
                    }
                }
                else {
                    if (recordExists || !speakerIdAlreadyExists) {
                        updateSpeaker.setEnabled(true);
                    }
                    if(!recordExists && currentSpeakerName.equals(originalSpeakerName)) {
                        updateSpeaker.setEnabled(false);
                    }
                }
            }
            else {
                updateSpeaker.setEnabled(false);
            }
        }
    };

    private View.OnClickListener updateSpeakerListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                Map<String, String> speakersList = SpeakersList.getList();
                SpeakersListSaveManager speakersListSaveManager = new SpeakersListSaveManager(EditSpeakerModelActivity.this);

                if (newSpeaker) {
                    //Create a speaker model with the data previously normalized with addAudio.
                    String newId = String.valueOf(SpeakersListSaveManager.getNewSpeakerId());

                    speakersList.put(currentSpeakerName, newId);
                    alizeSystem.createSpeakerModel(newId);

                    if (saveSpeakersModels()) {
                        alizeSystem.saveSpeakerModel(newId, newId);
                        speakersListSaveManager.addSpeaker(currentSpeakerName, newId);
                    }
                }
                else if (!originalSpeakerName.equals(currentSpeakerName)) {
                    speakersList.put(currentSpeakerName, speakersList.remove(originalSpeakerName));
                    if (saveSpeakersModels()) {
                        speakersListSaveManager.updateSpeakerName(originalSpeakerName, currentSpeakerName);
                    }

                    if (recordExists) {
                        //Change the model of the speaker with the id currentSpeakerName.
                        String speakerId = getSpeakerId(currentSpeakerName);
                        alizeSystem.adaptSpeakerModel(speakerId);

                        if (saveSpeakersModels()) {
                            alizeSystem.saveSpeakerModel(speakerId, speakerId);
                        }
                    }
                }
                else {
                    //Change the model of the speaker with the id currentSpeakerName.
                    String speakerId = getSpeakerId(currentSpeakerName);
                    alizeSystem.adaptSpeakerModel(speakerId);

                    if (saveSpeakersModels()) {
                        alizeSystem.saveSpeakerModel(speakerId, speakerId);
                    }
                }

                //Reset input, since we will not make any more use of this audio signal.
                alizeSystem.resetAudio();       //Reset the audio samples of the Alize system.
                alizeSystem.resetFeatures();    //Reset the features of the Alize system.
                finish();
            } catch (AlizeException e) {
                e.printStackTrace();
            } catch (IdAlreadyExistsException e) {
                e.printStackTrace();
            }
        }
    };

    protected void afterRecordProcessing() {
        if (emptyRecord) {
            updateSpeaker.setEnabled(false);
            makeToast(getResources().getString(R.string.no_sound_detected_recoloc));
        }
        else if (!currentSpeakerName.isEmpty() && !speakerIdAlreadyExists) {
            updateSpeaker.setEnabled(true);
        }
    }
}