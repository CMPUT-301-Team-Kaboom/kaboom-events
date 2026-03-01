package com.example.projecteventlotteryapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

// todo: create javadocs and add error handling

public class EventArrayAdapter extends ArrayAdapter<Event>  {
    private ArrayList<Event> events;
    private Context context;

    public EventArrayAdapter(Context context, ArrayList<Event> events) {
        super(context, 0, events);
        this.events = events;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        View view = convertView;
        if (view == null){
            view = LayoutInflater.from(context).inflate(R.layout.events_list_item, parent, false);
        }

        Event event = events.get(position);
        TextView organizerTextView = view.findViewById(R.id.tv_event_item_organizer);
        TextView nameTextView = view.findViewById(R.id.tv_event_item_name);
        TextView drawDateTextView = view.findViewById(R.id.tv_event_item_draw_date);
        TextView attendeesTextView = view.findViewById(R.id.tv_event_item_attendees);
        ImageView posterImageView = view.findViewById(R.id.iv_event_item_poster);

        nameTextView.setText(event.getName());
        /* i don't know how to get organizer yet: organizerTextView.setText(event.getOrganizer()); */
        DateTimeFormatter datePattern = DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a");
        String formattedDate = event.getDrawDate().format(datePattern);
        drawDateTextView.setText("Drawn on " + formattedDate);
        attendeesTextView.setText("Attendees: " + event.getAttendeesLimit());

        return view;
    }
}
