package fr.univavignon.alize.AndroidALIZEDemo;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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
    private String currentSpeakerName, originalSpeakerName, speakerId;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_speaker_model);

        try {
            speakers = demoSpkRecSystem.speakerIDs(); //Get all speakers name.
        }
        catch (AlizeException e) {
            e.printStackTrace();
        }

        currentSpeakerName = "";
        speakerId = getIntent().getStringExtra("speakerId");
        String speakerName = demoSpkRecSystem.getSpeakerName(speakerId);
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
                    if (demoSpkRecSystem.getSpeakerName(spkId).equals(currentSpeakerName) && !currentSpeakerName.equals(originalSpeakerName)) {
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
                if (newSpeaker) {
                    //Create a speaker model with the data previously normalized with addAudio.
                    demoSpkRecSystem.addAndCreateSpeakerModel(currentSpeakerName);
                }
                else if (!originalSpeakerName.equals(currentSpeakerName)) {
                    demoSpkRecSystem.setSpeakerName(speakerId, currentSpeakerName);

                    if (recordExists) {
                        //Change the model of the speaker with the id currentSpeakerName.
                        demoSpkRecSystem.adaptSpeakerModel(speakerId);
                    }
                }
                else {
                    //Change the model of the speaker with the id currentSpeakerName.
                    demoSpkRecSystem.adaptSpeakerModel(speakerId);
                }

                //Reset input, since we will not make any more use of this audio signal.
                demoSpkRecSystem.resetAudio();       //Reset the audio samples of the Alize system.
                demoSpkRecSystem.resetFeatures();    //Reset the features of the Alize system.

                finish();
            } catch (AlizeException e) {
                e.printStackTrace();
            } catch (IdAlreadyExistsException e) {
                e.printStackTrace();
            } catch (Throwable e) { //TODO catch proper exception
                e.printStackTrace();
                makeToast(getResources().getString(R.string.recording_not_completed));
            }
        }
    };

    protected void afterRecordProcessing() {
        if (emptyRecord) {
            updateSpeaker.setEnabled(false);
            makeToast(getResources().getString(R.string.no_sound_detected_recoloc));
        }
        else if (newSpeaker && !recordExists) {
            updateSpeaker.setEnabled(false);
        }
        else if (!currentSpeakerName.isEmpty() && !speakerIdAlreadyExists) {
            updateSpeaker.setEnabled(true);
        }
    }
}