package fr.univavignon.alize.AndroidALIZEDemo.speakerslist;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

public class SpeakerListAdapter extends ArrayAdapter<Speaker> {

    private List<Speaker> items;
    private int layoutResourceId;
    private Context context;

    public SpeakerListAdapter(Context context, int layoutResourceId, List<Speaker> items) {
        super(context, layoutResourceId, items);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row;
        SpeakerHolder holder;

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        row = inflater.inflate(layoutResourceId, parent, false);

        holder = new SpeakerHolder();
        holder.speaker = items.get(position);

        holder.update = row.findViewById(fr.univavignon.alize.AndroidALIZEDemo.R.id.update_button);
        holder.update.setTag(holder.speaker);
        holder.test = row.findViewById(fr.univavignon.alize.AndroidALIZEDemo.R.id.test_button);
        if (holder.speaker.getName().isEmpty())
            holder.test.setEnabled(false);
        else
            holder.test.setEnabled(true);
        holder.test.setTag(holder.speaker);
        holder.remove = row.findViewById(fr.univavignon.alize.AndroidALIZEDemo.R.id.remove_button);
        holder.remove.setTag(holder.speaker);

        holder.name = row.findViewById(fr.univavignon.alize.AndroidALIZEDemo.R.id.speaker_name);

        row.setTag(holder);

        setupItem(holder);
        return row;
    }

    private void setupItem(SpeakerHolder holder) {
        holder.name.setText(holder.speaker.getName());
    }

    public static class SpeakerHolder {
        Speaker speaker;
        TextView name;
        Button update;
        Button test;
        ImageButton remove;
    }
}
