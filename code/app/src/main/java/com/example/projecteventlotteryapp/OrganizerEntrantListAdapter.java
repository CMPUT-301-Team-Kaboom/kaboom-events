package com.example.projecteventlotteryapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class OrganizerEntrantListAdapter extends ArrayAdapter<String> {
    private Boolean isSelectionMode = false;
    private Set<Integer> selectedPositions = new HashSet<>();
    private Context context;
    private ArrayList<String> entrantList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void setSelectionMode(Boolean isSelectionMode) {
        this.isSelectionMode = isSelectionMode;
        notifyDataSetChanged();
    }

    public void selectAll() {
        for (int i = 0; i < entrantList.size(); i++) {
            selectedPositions.add(i);
        }
        notifyDataSetChanged();
    }

    public void clearSelection() {
        selectedPositions.clear();
        notifyDataSetChanged();
    }

    public OrganizerEntrantListAdapter(@NonNull Context context, ArrayList<String> entrantList) {
        super(context, 0, entrantList);
        this.context = context;
        this.entrantList = entrantList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.organizer_entrant_list_item, parent, false);
        }


        TextView itemCount = view.findViewById(R.id.tv_organizer_entrantList_item_number);
        TextView entrantName = view.findViewById(R.id.tv_organizer_entrantList_item_name);
        TextView entrantEmail = view.findViewById(R.id.tv_organizer_entrantList_item_email);

        DocumentReference userDoc = db.collection("entrants").document(entrantList.get(position));
        userDoc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                if (doc.exists()) {
                    itemCount.setText((position + 1) + "");
                    entrantName.setText(doc.getString("name"));
                    entrantEmail.setText(doc.getString("email"));
                }
            }
        });

        CheckBox checkBox = view.findViewById(R.id.cb_entrant_select);

        // Only show checkbox if in selection mode
        checkBox.setVisibility(isSelectionMode ? View.VISIBLE : View.GONE);

        // Prevent listener triggers during recycling
        checkBox.setOnCheckedChangeListener(null);
        checkBox.setChecked(selectedPositions.contains(position));

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) selectedPositions.add(position);
            else selectedPositions.remove(position);
    });
        return view;
    }
}
