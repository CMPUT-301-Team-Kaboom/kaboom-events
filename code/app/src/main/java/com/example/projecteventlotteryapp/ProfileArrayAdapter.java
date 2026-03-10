//reference: https://stackoverflow.com/questions/61830784/recyclerview-in-android-studio-error-null-object-reference#:~:text=Comments&text=You%20need%20to%20do%20two,use%20the%20Anonymous%20class%20here.
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

import java.util.ArrayList;

public class ProfileArrayAdapter extends ArrayAdapter<User> {
    private ArrayList<User> profiles;
    private Context context;
    private OnDeleteClickListener deleteListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(User user);
    }

    public ProfileArrayAdapter(Context context, ArrayList<User> profiles, OnDeleteClickListener deleteListener) {
        super(context, 0, profiles);
        this.profiles = profiles;
        this.context = context;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        View view = convertView;
        if (view == null){
            view = LayoutInflater.from(context).inflate(R.layout.admin_profiles_list_item, parent, false);
        }

        User profile = profiles.get(position);
        TextView profileItemPositionTextView = view.findViewById(R.id.tv_profile_item_position);
        ImageView profileIconImageView = view.findViewById(R.id.iv_profile_item_icon);
        TextView profileNameTextView = view.findViewById(R.id.tv_profile_item_name);
        TextView profileEmailTextView = view.findViewById(R.id.tv_profile_item_email);
        ImageView profileDeleteIcon = view.findViewById(R.id.iv_profile_item_delete);

        profileItemPositionTextView.setText(String.valueOf(position + 1));
        profileNameTextView.setText(profile.getName());
        profileEmailTextView.setText(profile.getEmail());

        profileDeleteIcon.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(profile);
            }
        });

        return view;
    }
}
