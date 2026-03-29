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

import com.example.projecteventlotteryapp.Models.User;

import java.util.ArrayList;
/**
 * Custom adapter class that handles displaying events as list items.
 */
public class PrivateInviteEntrantArrayAdapter extends ArrayAdapter<User> {
    private ArrayList<User> entrants;
    private Context context;
    OnShareClickListener listener;

    public interface OnShareClickListener {
        void onShareClick(User user);
    }
    public PrivateInviteEntrantArrayAdapter(Context context, ArrayList<User> entrants, OnShareClickListener listener) {
        super(context, 0, entrants);
        this.entrants = entrants;
        this.context = context;
        this.listener = listener;
    }

    /**
     * Handles the layout display of a list of entrants you can share a private event with.
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
            view = LayoutInflater.from(context).inflate(R.layout.entrants_invite_item, parent, false);
        }

        User entrant = entrants.get(position);
        TextView entrantNameTextView = view.findViewById(R.id.tv_entrant_item_name);
        TextView entrantEmailTextView = view.findViewById(R.id.tv_entrant_item_email);
        TextView entrantPhoneTextView = view.findViewById(R.id.tv_entrant_item_phone);
        ImageView entrantInviteIcon = view.findViewById(R.id.iv_entrant_item_invite);

        entrantNameTextView.setText(entrant.getName());
        entrantEmailTextView.setText(entrant.getEmail());
        entrantPhoneTextView.setText(entrant.getPhoneNumber());

        entrantInviteIcon.setOnClickListener(v -> {
            if (listener != null) {
                listener.onShareClick(entrant);
            }
        });

        return view;
    }
}
