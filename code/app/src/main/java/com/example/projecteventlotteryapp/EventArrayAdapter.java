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

import com.bumptech.glide.Glide;
import com.example.projecteventlotteryapp.Models.Event;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Custom adapter class that handles displaying events as list items.
 */
public class EventArrayAdapter extends ArrayAdapter<Event>  {
    private ArrayList<Event> events;
    private Context context;

    public EventArrayAdapter(Context context, ArrayList<Event> events) {
        super(context, 0, events);
        this.events = events;
        this.context = context;
    }

    /**
     * Handles the layout display of a list of events.
     *
     * @param position The position of the item within the adapter's data set of the item whose view
     *        we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *        is non-null and of an appropriate type before using. If it is not possible to convert
     *        this view to display the correct data, this method can create a new view.
     *        Heterogeneous lists can specify their number of view types, so that this View is
     *        always of the right type (see {@link #getViewTypeCount()} and
     *        {@link #getItemViewType(int)}).
     * @param parent The parent that this view will eventually be attached to
     * @return View to be displayed
     */
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
        organizerTextView.setText(event.getOrganizerName());
        DateTimeFormatter datePattern = DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a");
        String formattedDate = event.getDrawDate().format(datePattern);
        drawDateTextView.setText("Starts: " + formattedDate);
        attendeesTextView.setText("Attendees: " + event.getAttendeesLimit());
        if (event.getPoster() != null) {
            Glide.with(context).load(event.getPoster()).into(posterImageView);
        } else {
            Glide.with(context).load(R.drawable.default_poster).into(posterImageView);
        }
        return view;
    }
}
