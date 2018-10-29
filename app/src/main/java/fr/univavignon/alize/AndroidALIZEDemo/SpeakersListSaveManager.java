package fr.univavignon.alize.AndroidALIZEDemo;

import android.content.Context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import AlizeSpkRec.AlizeException;
import AlizeSpkRec.SimpleSpkDetSystem;

public class SpeakersListSaveManager {

    private static int speakerId = 0;
    private Context context;
    private String pathToGmm;
    private String gmmExt;
    private SimpleSpkDetSystem alizeSystem;
    private String saveSpeakersListFile;
    private String saveSpeakersListFileBaseName = "speakersList";

    public SpeakersListSaveManager(Context context) {
        this.context = context;
        String globalPath = context.getFilesDir().getPath() + File.separator;
        gmmExt = ".gmm"; //TODO get ext from config
        pathToGmm = globalPath + "gmm" + File.separator; //TODO get directory from config
        saveSpeakersListFile = globalPath + saveSpeakersListFileBaseName + ".txt";
    }

    public void addSpeaker(String speakerName, String speakerId) {
        addSpeaker(speakerName, speakerId, true);
    }

    public void addSpeaker(String speakerName, String speakerId, boolean append) {
        writeToFile(speakerName + ":" + speakerId + "\n", append);
    }

    public void updateSpeakerName(String originalSpeakerName, String newSpeakerName) {
        String fileContent = readAllLines();
        if (fileContent.contains(originalSpeakerName)) {
            fileContent = fileContent.replace(originalSpeakerName, newSpeakerName);
        }
        writeToFile(fileContent, false);
    }

    public void removeSpeaker(String speakerId, String speakerName) {
        //remove speaker from the list
        String fileContent = readAllLines();
        int index = fileContent.indexOf(speakerName);
        String deletePart = fileContent.substring(index, fileContent.indexOf("\n", index)+1);
        fileContent = fileContent.replace(deletePart, "");
        writeToFile(fileContent, false);

        //Delete speaker model file
        File file = new File(pathToGmm + speakerId + gmmExt);
        file.delete();
    }

    public void loadSavedModels() throws IOException, AlizeException {
        SpeakersList.reset();
        alizeSystem = SharedAlize.getInstance(context); //TODO need to be reset too
        Map<String, String> speakersList = SpeakersList.getList();

        for (String speaker : readAllLines().split("\n")) {
            String speakerName = speaker.split(":")[0];
            String speakerId = speaker.split(":")[1];

            alizeSystem.loadSpeakerModel(speakerId, speakerId);
            speakersList.put(speakerName, speakerId);
        }
    }

    public static int getNewSpeakerId() {
        return speakerId++;
    }

    private void writeToFile(String content, boolean append) {
        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;
        try {
            fileWriter = new FileWriter(saveSpeakersListFile, append);
            bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(content);

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

    private String readAllLines() {
        StringBuilder result = new StringBuilder();
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        
        try {
            fileReader = new FileReader(saveSpeakersListFile);
            bufferedReader = new BufferedReader(fileReader);
            String sCurrentLine;

            while ((sCurrentLine = bufferedReader.readLine()) != null) {
                result.append(sCurrentLine).append("\n");
            }

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            try {
                if (bufferedReader != null)
                    bufferedReader.close();

                if (fileReader != null)
                    fileReader.close();

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result.toString();
    }
}
