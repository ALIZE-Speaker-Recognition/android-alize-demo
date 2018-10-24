package fr.univavignon.alize.AndroidALIZEDemo;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import AlizeSpkRec.AlizeException;
import AlizeSpkRec.SimpleSpkDetSystem;

/**
 * Activity meant to identify or verify a speaker.
 * Identify is used when the system try to recognize a speaker in the system with the current record.
 * Verify is used when the system is listening to the current record and determine if it's the right speaker or not.
 *
 * @author Nicolas Duret
 */
public class VerificationActivity extends RecordActivity {

    private final int ERROR_COLOR = Color.RED;
    private final int SUCCESS_COLOR = Color.rgb(0,150,0);

    private String speakerName;
    private TextView resultText;
    private boolean identify = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.verification);

            speakerName = getIntent().getStringExtra("speakerName");
            resultText = findViewById(R.id.result_text);
            startRecordButton = findViewById(R.id.startBtn);
            stopRecordButton = findViewById(R.id.stopBtn);
            timeText = findViewById(R.id.timeText);

            String title = "Verify '" + speakerName + "' Model";
            if (speakerName.isEmpty()) {
                identify = true;
                title = getResources().getString(R.string.identify_speaker);
            }
            setTitle(title);

            startRecordButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    resultText.setText("");
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
        catch (Throwable e) {
            e.printStackTrace();
        }
    }

    protected void afterRecordProcessing() {
        String result = "Error";

        if (identify) {
            //Try to match a speaker with the record

            try {
                //This is the other main task of speaker recognition: compare a recording with all the speakers
                //known to the system in order to determine whose voice it is (or reject it as unknown).
                SimpleSpkDetSystem.SpkRecResult identificationResult = alizeSystem.identifySpeaker();

                if (identificationResult.match) {
                    result = "Match:\n" + identificationResult.speakerId + "\nScore:\n" + identificationResult.score;
                    resultText.setTextColor(SUCCESS_COLOR);
                }
                else {
                    result = "No Match\nScore:\n" + identificationResult.score;
                    resultText.setTextColor(ERROR_COLOR);
                }
            }
            catch (AlizeException e) {
                e.printStackTrace();
            }
        }
        else {
            //Compare the record with the speaker model

            try {
                //Compare the audio signal with the speaker model we created earlier.
                SimpleSpkDetSystem.SpkRecResult verificationResult = alizeSystem.verifySpeaker(speakerName);

                if (verificationResult.match) {
                    result = "Match\nScore:\n" + verificationResult.score;
                    resultText.setTextColor(SUCCESS_COLOR);
                }
                else {
                    result = "No Match\nScore:\n" + verificationResult.score;
                    resultText.setTextColor(ERROR_COLOR);
                }
            }
            catch (AlizeException e) {
                e.printStackTrace();
            }
        }
        resultText.setText(result);

        try {
            //Reset input, since we will not make any more use of this audio signal.
            alizeSystem.resetAudio();       //Reset the audio samples of the Alize system.
            alizeSystem.resetFeatures();    //Reset the features of the Alize system.
        } catch (AlizeException e) {
            e.printStackTrace();
        }
    }
}
