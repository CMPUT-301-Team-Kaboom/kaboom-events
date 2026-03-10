package com.example.projecteventlotteryapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

//todo: put in profile pictures once we have them in the User class

public class OrganizerArrayAdapter extends ArrayAdapter<User> {
    private ArrayList<User> organizers;
    private Context context;
    public OrganizerArrayAdapter(Context context, ArrayList<User> organizers) {
        super(context, 0, organizers);
        this.organizers = organizers;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.admin_organizers_list_item, parent, false);
        }
        User organizer = organizers.get(position);
        TextView organizerItemPositionTextView = view.findViewById(R.id.tv_organizer_item_position);
        ImageView organizerIconImageView = view.findViewById(R.id.iv_organizer_item_icon);
        TextView organizerNameTextView = view.findViewById(R.id.tv_organizer_item_name);
        TextView organizerEmailTextView = view.findViewById(R.id.tv_organizer_item_email);

        organizerItemPositionTextView.setText(String.valueOf(position + 1));
        organizerNameTextView.setText(organizer.getName());
        organizerEmailTextView.setText(organizer.getEmail());

        return view;
    }

}
