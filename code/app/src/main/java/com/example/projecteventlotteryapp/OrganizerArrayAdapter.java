// reference: https://stackoverflow.com/questions/61830784/recyclerview-in-android-studio-error-null-object-reference#:~:text=Comments&text=You%20need%20to%20do%20two,use%20the%20Anonymous%20class%20here.
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

/**
 * Custom array adapter for displaying a list of user objects specifically representing
 * organizers within the admin view
 * This adapter populates a list item with the organizer's name, email, and their position in the
 * list. it also provides a functional delete icon and a notifications button
 * The notifications button will lead to an activity that contains all the notifications that
 * organizer has sent in a list
 */
public class OrganizerArrayAdapter extends ArrayAdapter<User> {
    private ArrayList<User> organizers;
    private Context context;
    private OnDeleteClickListener deleteListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(User organizer);
    }

    public OrganizerArrayAdapter(Context context, ArrayList<User> organizers, OnDeleteClickListener deleteListener) {
        super(context, 0, organizers);
        this.organizers = organizers;
        this.context = context;
        this.deleteListener = deleteListener;
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
        TextView organizerNameTextView = view.findViewById(R.id.tv_organizer_item_name);
        TextView organizerEmailTextView = view.findViewById(R.id.tv_organizer_item_email);
        ImageView deleteIcon = view.findViewById(R.id.iv_organizer_item_delete);
        Button notificationsBtn = view.findViewById(R.id.btn_organizer_item_notifications);
        
        organizerItemPositionTextView.setText(String.valueOf(position + 1));
        organizerNameTextView.setText(organizer.getName());
        organizerEmailTextView.setText(organizer.getEmail());

        deleteIcon.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(organizer);
            }
        });

        return view;
    }
}
