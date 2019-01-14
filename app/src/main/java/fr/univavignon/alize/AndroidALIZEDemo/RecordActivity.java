package fr.univavignon.alize.AndroidALIZEDemo;

import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import AlizeSpkRec.AlizeException;

import static android.Manifest.permission.RECORD_AUDIO;

/**
 * This class is meant to be used by activities that want to use Alize record features.
 *
 * @author Nicolas Duret
 */
public class RecordActivity extends BaseActivity {

    protected static final int RECORDER_SAMPLERATE = 8000;
    protected static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    protected static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    protected int bufferElements2Rec = 2000;
    protected int bytesPerElement = 2; // 2 bytes in 16bit format

    protected long startTime;
    protected TextView timeText;
    protected boolean emptyRecord = false;
    protected boolean recordExists = false;
    protected boolean recordError = false;
    protected AudioRecord recorder = null;
    protected Button startRecordButton, stopRecordButton;
    protected Thread recordingThread = null, addSamplesThread = null, timerThread = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected boolean checkPermission() {
        return ContextCompat.checkSelfPermission(getApplicationContext(),
                RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    protected void requestPermission() {
        ActivityCompat.requestPermissions(RecordActivity.this, new
                String[]{RECORD_AUDIO}, 42);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 42: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startRecording();
                }
                else {
                    makeToast(getResources().getString(R.string.permission_error_message));
                }
                break;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            try {
                //Reset input, since we will not make any more use of this audio signal.
                demoSpkRecSystem.resetAudio();       //Reset the audio samples of the Alize system.
                demoSpkRecSystem.resetFeatures();    //Reset the features of the Alize system.
            } catch (AlizeException e) {
                e.printStackTrace();
            }
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Method that start the record of the android microphone and use Alize features.
     */
    protected void startRecording() {
        if (!checkPermission()) {
            requestPermission();
            return;
        }

        emptyRecord = true;
        recordError = false;
        startRecordButton.setVisibility(View.INVISIBLE);
        stopRecordButton.setVisibility(View.VISIBLE);
        timeText.setText(R.string.default_time);

        //Start the record
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING, bufferElements2Rec * bytesPerElement);
        recorder.startRecording();

        if (recordExists) {
            try {
                //Reset input, since we will not make any more use of this audio signal.
                demoSpkRecSystem.resetAudio();       //Reset the audio samples of the Alize system.
                demoSpkRecSystem.resetFeatures();    //Reset the features of the Alize system.
            } catch (AlizeException e) {
                e.printStackTrace();
            }
            recordExists = false;
        }

        final List<short[]> audioPackets = Collections.synchronizedList(new ArrayList<short[]>());

        //This thread is meant to record the audio samples with android recorder.
        recordingThread = new Thread(new Runnable() {
            private Handler handler = new Handler();

            @Override
            public void run() {
                startTime = System.currentTimeMillis();

                short[] tmpAudioSamples = new short[bufferElements2Rec];
                while (recorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                    int samplesRead = recorder.read(tmpAudioSamples, 0, bufferElements2Rec);

                    if (samplesRead > 0) {
                        short[] samples = new short[samplesRead];
                        //System.arraycopy(tmpAudioSamples, 0, samples, 0, samplesRead);
                        for (int i=0; i < samples.length; i++) {
                            samples[i] = tmpAudioSamples[i];
                            if (samples[i] != 0) {
                                emptyRecord = false;
                            }
                        }

                        synchronized (audioPackets) {
                            audioPackets.add(samples);
                        }
                    }
                }
            }
        }, "AudioRecorder Thread");

        //This thread is meant to use the audio samples and send them to the Alize system.
        addSamplesThread = new Thread(new Runnable() {
            private Handler handler = new Handler();

            @Override
            public void run() {
                short[] nextElement;

                while ((recorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING)
                        || (!audioPackets.isEmpty())) {
                    nextElement = null;

                    synchronized (audioPackets) {
                        if (!audioPackets.isEmpty()) {
                            nextElement = audioPackets.get(0);
                            audioPackets.remove(0);
                        }
                    }
                    if (nextElement != null) {
                        try {
                            //Receive an audio signal as 16-bit signed integer linear PCM, parameterize it and add it to the feature server.
                            demoSpkRecSystem.addAudio(nextElement);
                        } catch (AlizeException e) {
                            e.printStackTrace();
                            recordError = true;
                        } catch (Throwable e) { //TODO catch proper exception
                            e.printStackTrace();
                            recordError = true;
                        }
                    }
                }

                try {
                    recordingThread.join(); //Wait the recordingThread to end.
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                while (!audioPackets.isEmpty()) {
                    nextElement = audioPackets.get(0);
                    audioPackets.remove(0);

                    if (nextElement != null) {
                        try {
                            demoSpkRecSystem.addAudio(nextElement);
                        } catch (AlizeException e) {
                            e.printStackTrace();
                            recordError = true;
                        } catch (Throwable e) { //TODO catch proper exception
                            e.printStackTrace();
                            recordError = true;
                        }
                    }
                }
            }
        }, "addSamples Thread");

        //This thread is meant to increase the timer with the current time.
        timerThread = new Thread(new Runnable() {
            private Handler handler = new Handler();

            @Override
            public void run() {
                while((recorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING)) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            long currentTime = System.currentTimeMillis() - startTime;
                            String result = new SimpleDateFormat("mm:ss:SS", defaultLanguage)
                                    .format(new Date(currentTime));
                            timeText.setText(result);
                        }
                    });
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, "Timer Thread");

        recordingThread.start();
        addSamplesThread.start();
        timerThread.start();
    }

    /**
     * Stop the record and reset record features.
     */
    protected void stopRecording() {
        stopRecordButton.setVisibility(View.INVISIBLE);

        if (recorder != null) {
            recorder.stop();
            try {
                recordingThread.join();
                addSamplesThread.join();
                timerThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            recorder.release();
            recorder = null;
            recordExists = !recordError;
            recordingThread = null;
            addSamplesThread = null;
            startRecordButton.setVisibility(View.VISIBLE);

            String resultText = getResources().getString(R.string.recording_completed);
            if (recordError) {
                resultText = getResources().getString(R.string.recording_not_completed);
            }
            makeToast(resultText);
            afterRecordProcessing();
        }
    }

    /**
     * Method meant to be inherited and implemented with the post recording processes.
     */
    protected void afterRecordProcessing() {}
}
