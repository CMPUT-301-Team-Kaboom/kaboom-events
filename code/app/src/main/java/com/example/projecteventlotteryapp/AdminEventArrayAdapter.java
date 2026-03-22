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

import com.example.projecteventlotteryapp.Models.Event;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class AdminEventArrayAdapter extends ArrayAdapter<Event> {
    private ArrayList<Event> events;
    private Context context;
    OnDeleteClickListener deleteListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(Event event);
    }

    public AdminEventArrayAdapter(Context context, ArrayList<Event> events, OnDeleteClickListener deleteListener) {
        super(context, 0, events);
        this.events = events;
        this.context = context;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.admin_event_list_item, parent, false);
        }

        Event event = events.get(position);
        TextView organizerTextView = view.findViewById(R.id.tv_admin_event_item_organizer);
        TextView nameTextView = view.findViewById(R.id.tv_admin_event_item_name);
        TextView drawDateTextView = view.findViewById(R.id.tv_admin_event_item_draw_date);
        TextView attendeesTextView = view.findViewById(R.id.tv_admin_event_item_attendees);
        ImageView posterImageView = view.findViewById(R.id.iv_admin_event_item_poster);
        ImageView deleteIcon = view.findViewById(R.id.iv_admin_event_item_delete);

        nameTextView.setText(event.getName());
        organizerTextView.setText(event.getOrganizerName());
        
        if (event.getDrawDate() != null) {
            DateTimeFormatter datePattern = DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a");
            String formattedDate = event.getDrawDate().format(datePattern);
            drawDateTextView.setText("Starts on " + formattedDate);
        } else {
            drawDateTextView.setText("Start date TBD");
        }
        
        attendeesTextView.setText("Attendees: " + event.getAttendeesLimit());

        deleteIcon.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(event);
            }
        });

        return view;
    }
}
