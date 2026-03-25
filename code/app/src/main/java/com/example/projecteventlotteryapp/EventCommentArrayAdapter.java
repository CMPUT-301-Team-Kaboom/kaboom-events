package com.example.projecteventlotteryapp;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.projecteventlotteryapp.Enums.Role;
import com.example.projecteventlotteryapp.dbUtils.EventUtils;
import com.example.projecteventlotteryapp.dbUtils.FirestoreUtils;
import com.example.projecteventlotteryapp.dbUtils.UserUtils;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Document;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EventCommentArrayAdapter extends ArrayAdapter<String> {
    private Context context;
    private ArrayList<String> comments;
    private FirebaseFirestore db;
    private UserUtils userUtils;
    private Role userRole;
    private EventUtils eventUtils;
    public EventCommentArrayAdapter(@NonNull Context context, ArrayList<String> comments, Role userRole){
        super(context, 0, comments);
        this.context = context;
        this.comments = comments;
        this.userRole = userRole;
        this.db = FirebaseFirestore.getInstance();
        userUtils = new UserUtils(db);
        eventUtils = new EventUtils(db);

        Log.d("CommentAdapter", "Comments: " + comments.size());
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null){
            view = LayoutInflater.from(context).inflate(R.layout.event_comment, parent, false);
        }

        TextView username = view.findViewById(R.id.tv_comment_username);
        TextView date = view.findViewById(R.id.tv_comment_datetime);
        TextView text = view.findViewById(R.id.tv_comment_text);
        ImageButton deleteBtn = view.findViewById(R.id.btn_comment_delete);

        DocumentReference commentDoc = db.collection("comments").document(comments.get(position));

        commentDoc.get().addOnCompleteListener(commentTask -> {
            if (!commentTask.isSuccessful()){
                Log.e("Comments", "Failed to fetch comments for comment: " + comments.get(position));
            }
            DocumentSnapshot comment = commentTask.getResult();
            if (!comment.exists()){
                Log.e("Comments", "Comment does not exist: " + comments.get(position));
            }

            LocalDateTime commentDateTime = FirestoreUtils.fetchLocalDateTime(comment, "timestamp");
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM. dd, yyyy HH:mm");

            date.setText(dateFormatter.format(commentDateTime));
            text.setText(comment.getString("text"));

            Task<DocumentSnapshot> entrantComment = userUtils.loadUserProfile(comment.getString("userID"), Role.ENTRANT);
            Task<DocumentSnapshot> organizerComment = userUtils.loadUserProfile(comment.getString("userID"), Role.ORGANIZER);

            // make sure that both users are fetched before setting the name
            Tasks.whenAllSuccess(entrantComment, organizerComment).addOnSuccessListener(userTasks -> {
                DocumentSnapshot entrant = (DocumentSnapshot) userTasks.get(0);
                DocumentSnapshot organizer = (DocumentSnapshot) userTasks.get(1);
                if (entrant.exists()){
                    username.setText(entrant.getString("name"));
                } else if (organizer.exists()){
                    username.setText(organizer.getString("name"));
                } else {
                    username.setText("deleted user"); // default if the user does not exist
                }
            });
        });

        Log.d("CommentsActivity", userRole.toString());

        if (userRole == Role.ORGANIZER || userRole == Role.ADMIN) {
            deleteBtn.setVisibility(VISIBLE);
            deleteBtn.setOnClickListener(v -> {
                new AlertDialog.Builder(context, R.style.DeleteGuard)
                        .setTitle("Delete Comment")
                        .setMessage("Are you sure you want to delete this comment?")
                        .setPositiveButton("Delete", ((dialog, which) -> {
                            eventUtils.deleteCommentFromEvent(comments.get(position));
                            comments.remove(position);
                            notifyDataSetChanged();
                        }))
                        .setNegativeButton("Cancel", ((dialog, which) -> dialog.dismiss()))
                        .show();
            });
        } else {
            deleteBtn.setVisibility(GONE);
        }

        return view;
    }
}
