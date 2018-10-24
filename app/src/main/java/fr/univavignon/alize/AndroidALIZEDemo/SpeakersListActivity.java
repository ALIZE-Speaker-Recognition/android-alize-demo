package fr.univavignon.alize.AndroidALIZEDemo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import AlizeSpkRec.AlizeException;
import fr.univavignon.alize.AndroidALIZEDemo.speakerslist.Speaker;
import fr.univavignon.alize.AndroidALIZEDemo.speakerslist.SpeakerListAdapter;

/**
 * This activity is meant to manage the speakers list.
 *
 * @author Nicolas Duret
 */
public class SpeakersListActivity extends BaseActivity {

    private String[] speakers;
    private TextView noSpeakers;
    private SpeakerListAdapter adapter;
    private Button identifyButton, removeAll;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.speakers_list);

            //Setup the ListViewAdapter
            adapter = new SpeakerListAdapter(SpeakersListActivity.this, R.layout.list_item, new ArrayList<Speaker>());
            ListView speakerListView = findViewById(R.id.speakerListView);
            speakerListView.setAdapter(adapter);

            speakers = alizeSystem.speakerIDs();    //Get all speakers name.
            noSpeakers = findViewById(R.id.no_speakers);
            removeAll = findViewById(R.id.removeall_button);
            identifyButton = findViewById(R.id.identify_button);
            noSpeakers.setVisibility(View.INVISIBLE);

            clearAndFill();
            updateListViewContent();

        } catch (AlizeException e) {
            e.printStackTrace();
        }
    }

    public void updateSpeakerOnClickHandler(View v) {
        final Speaker itemToUpdate = (Speaker)v.getTag();
        startActivity(EditSpeakerModelActivity.class, new HashMap<String, Object>(){{
            put("speakerName", itemToUpdate.getName());
        }});
    }

    public void testSpeakerOnClickHandler(View v) {
        final Speaker itemToTest = (Speaker)v.getTag();
        startActivity(VerificationActivity.class, new HashMap<String, Object>(){{
            put("speakerName", itemToTest.getName());
        }});
    }

    public void removeSpeakerOnClickHandler(View v) {
        Speaker itemToRemove = (Speaker)v.getTag();
        String speakerId = itemToRemove.getName();
        try {
            if (!speakerId.isEmpty()) {
                //Remove the speaker speakerId from the Alize system.
                alizeSystem.removeSpeaker(speakerId);
            }
            updateSpeakersListObject();
        } catch (AlizeException e) {
            System.out.println(e.getMessage());
            makeToast(getString(R.string.error_remove_speaker));
        }
        adapter.remove(itemToRemove);
        updateListViewContent();
    }

    public void addSpeaker(View v) {
        startActivity(EditSpeakerModelActivity.class, new HashMap<String, Object>(){{
            put("speakerName", "");
        }});
    }

    public void identify(View v) {
        startActivity(VerificationActivity.class, new HashMap<String, Object>(){{
            put("speakerName", "");
        }});
    }

    public void removeAll(View v) {
        //TODO find why removeAllSpeakers() doesn't works
        try {
            //Remove all the speakers from the Alize system.
            alizeSystem.removeAllSpeakers();
            adapter.clear();
            updateSpeakersListObject();
            updateListViewContent();
        } catch (AlizeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        try {
            super.onResume();
            clearAndFill();
            updateListViewContent();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * Clear the list and fill it.
     */
    private void clearAndFill() throws AlizeException {
        updateSpeakersListObject();
        if (speakers.length == 0) {
            return;
        }
        adapter.clear();
        for (String speakerId : speakers) {
            adapter.insert(new Speaker(speakerId), adapter.getCount());
        }
    }

    /**
     * Update elements of the activity.
     */
    private void updateListViewContent() {
        if (speakers.length == 0) {
            if (adapter.getCount() == 0) {
                noSpeakers.setVisibility(View.VISIBLE);
                identifyButton.setEnabled(false);
                removeAll.setEnabled(false);
            }
            else
                noSpeakers.setVisibility(View.INVISIBLE);
        }
        else {
            noSpeakers.setVisibility(View.INVISIBLE);
            identifyButton.setEnabled(true);
            removeAll.setEnabled(true);
        }
    }

    private void updateSpeakersListObject() throws AlizeException {
        speakers = alizeSystem.speakerIDs(); //Get all speakers name.
    }

}
