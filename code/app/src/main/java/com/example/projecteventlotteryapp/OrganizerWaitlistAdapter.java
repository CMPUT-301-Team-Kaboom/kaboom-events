package com.example.projecteventlotteryapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class OrganizerWaitlistAdapter extends ArrayAdapter<User> {
    private Context context;
    private ArrayList<User> entrantList;
    public OrganizerWaitlistAdapter(@NonNull Context context, ArrayList<User> entrantList){
        super(context, 0, entrantList);
        this.context = context;
        this.entrantList = entrantList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null){
            view = LayoutInflater.from(context).inflate(R.layout.organizer_waitlist_item, parent, false);
        }

        User user = getItem(position);
        TextView itemCount = view.findViewById(R.id.tv_organizer_waitlist_item_number);
        TextView entrantName = view.findViewById(R.id.tv_organizer_waitlist_item_name);
        TextView entrantEmail = view.findViewById(R.id.tv_organizer_waitlist_item_email);

        itemCount.setText((position + 1) + "");
        entrantName.setText(user.getName());
        entrantEmail.setText(user.getEmail());

        return view;
    }
}
