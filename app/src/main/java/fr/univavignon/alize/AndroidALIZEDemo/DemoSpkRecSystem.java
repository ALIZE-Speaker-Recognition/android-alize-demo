package fr.univavignon.alize.AndroidALIZEDemo;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import AlizeSpkRec.AlizeException;
import AlizeSpkRec.IdAlreadyExistsException;
import AlizeSpkRec.SimpleSpkDetSystem;

/**
 * Singleton meant to get the instance of the Alize system without initializing it every time.
 *
 * @author Nicolas Duret
 */
public class DemoSpkRecSystem extends SimpleSpkDetSystem {

    private static int nextSpeakerId = 0;
    private static DemoSpkRecSystem sharedSystem;
    private String nameListFilePath;
    private Map<String, String> nameList;
    private SharedPreferences sharedPreferences;

    private DemoSpkRecSystem(InputStream configInput, String workdirPath) throws AlizeException, IOException {
        super(configInput, workdirPath);
    }

    public static DemoSpkRecSystem getSharedInstance(Context appContext) throws IOException, AlizeException {
        if (sharedSystem == null) {
            // We create a new spk det system with a config (extracted from the assets) and a directory where it can store files (models + temporary files)
            InputStream configAsset = appContext.getAssets().open("AlizeDefault.cfg");
            sharedSystem = new DemoSpkRecSystem(configAsset, appContext.getFilesDir().getPath());
            configAsset.close();

            // We also load the background model from the application assets
            InputStream backgroundModelAsset = appContext.getAssets().open("gmm/world.gmm");
            sharedSystem.loadBackgroundModel(backgroundModelAsset);
            backgroundModelAsset.close();

            sharedSystem.nameList = new HashMap<>();
            sharedSystem.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext);
            sharedSystem.nameListFilePath = appContext.getFilesDir().getPath() + File.separator + "speakers.spklst";

            File f = new File(sharedSystem.nameListFilePath);
            if(!f.exists()) {
                sharedSystem.saveList();
            }
            else {
                nextSpeakerId = sharedSystem.getLastSpeakerID();
            }
        }
        return sharedSystem;
    }

    public void setSpeakerName(String id, String name) {
        nameList.put(id, name);

        if (shouldKeepStateOnDisk()) {
            saveList();
        }
    }

    public String getSpeakerName(String id) {
        if (id.isEmpty()) {return "";}
        return nameList.get(id);
    }

    public void syncToDisc() throws AlizeException {
        saveList();
        for (Map.Entry<String, String> entry : nameList.entrySet()) {
            super.saveSpeakerModel(entry.getKey(), entry.getKey());
        }
    }

    public void loadSavedModels() throws AlizeException, IOException {
        loadList();

        removeAllSpeakers();
        for (Map.Entry<String, String> entry : nameList.entrySet()) {
            super.loadSpeakerModel(entry.getKey(), entry.getKey());
        }
    }

    public String addAndCreateSpeakerModel(String name) throws IdAlreadyExistsException, AlizeException {
        String newId = String.valueOf(nextSpeakerId++);
        super.createSpeakerModel(newId);
        nameList.put(newId, name);

        if (shouldKeepStateOnDisk()) {
            super.saveSpeakerModel(newId, newId);
            saveList();
        }
        return newId;
    }

    @Override
    public void adaptSpeakerModel(String speakerId) throws AlizeException {
        super.adaptSpeakerModel(speakerId);

        if (shouldKeepStateOnDisk()) {
            super.saveSpeakerModel(speakerId, speakerId);
        }
    }

    public void removeSpeaker(String speakerId) throws AlizeException {
        boolean keepStateOnDisk = shouldKeepStateOnDisk();
        super.removeSpeaker(speakerId/*, keepStateOnDisk*/); //TODO not implemented yet

        if (keepStateOnDisk) {
            nameList.remove(speakerId);
            saveList();
        }
    }

    @Override
    public void removeAllSpeakers() throws AlizeException {
        //TODO complete this
        //super.removeAllSpeakers();
    }




    private boolean shouldKeepStateOnDisk() {
        return sharedPreferences.getBoolean("save_speakers_models", true);
    }

    private void saveList() {
        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;
        try {
            fileWriter = new FileWriter(nameListFilePath);
            bufferedWriter = new BufferedWriter(fileWriter);

            for (Map.Entry<String, String> entry : nameList.entrySet()) {
                bufferedWriter.write(entry.getKey()+":"+entry.getValue()+"\n");
            }

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            try {
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }

                if (fileWriter != null) {
                    fileWriter.close();
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void loadList() {
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;

        try {
            String lineContent;
            fileReader = new FileReader(nameListFilePath);
            bufferedReader = new BufferedReader(fileReader);

            nameList = new HashMap<>();

            while ((lineContent = bufferedReader.readLine()) != null) {
                String speakerId = lineContent.split(":")[0];
                String speakerName = lineContent.split(":")[1];

                nameList.put(speakerId, speakerName);
            }

        } catch (IOException e) {
            e.printStackTrace();

        }
        finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }

                if (fileReader != null) {
                    fileReader.close();
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private int getLastSpeakerID() {
        int lastId = -1;
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;

        try {
            String lineContent;
            fileReader = new FileReader(nameListFilePath);
            bufferedReader = new BufferedReader(fileReader);

            while ((lineContent = bufferedReader.readLine()) != null) {
                int speakerId = Integer.parseInt(lineContent.split(":")[0]);

                if (speakerId > lastId) {
                    lastId = speakerId;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }

                if (fileReader != null) {
                    fileReader.close();
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        if (lastId == -1) {
            return 0;
        }
        return ++lastId;
    }
}
