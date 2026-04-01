package com.example.projecteventlotteryapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projecteventlotteryapp.EventCommentArrayAdapter;
import com.example.projecteventlotteryapp.Models.MyApp;
import com.example.projecteventlotteryapp.Models.User;
import com.example.projecteventlotteryapp.R;
import com.example.projecteventlotteryapp.dbUtils.EventUtils;
import com.example.projecteventlotteryapp.dbUtils.FirestoreUtils;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Displays all comments that were posted under the event
 *
 * This event allows both entrants and the event organizer to post new comments and view
 * other users' comments under the event.
 */
public class EventCommentsActivity extends BaseActivity {
    private String eventId;
    private ArrayList<String> commentsList;
    private EventCommentArrayAdapter adapter;
    private ListView commentsLV;
    private ImageButton backBtn;
    private Button postCommentBtn;
    private EditText commentTextbox;
    private FirebaseFirestore db;
    private DocumentReference eventDoc;
    private User globalUser;
    private EventUtils eventUtils;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_comments);

        Intent intent = getIntent();
        eventId = intent.getStringExtra("eventId");
        db = FirebaseFirestore.getInstance();
        eventUtils = new EventUtils(db);

        commentsLV = findViewById(R.id.lv_event_comments_list);
        backBtn = findViewById(R.id.btn_event_comments_back);
        postCommentBtn = findViewById(R.id.btn_event_comments_post_comment);
        commentTextbox = findViewById(R.id.et_event_comments_textbox);

        MyApp app = (MyApp) getApplication();
        globalUser = app.getCurrentUser();

        eventDoc = db.collection("events").document(eventId);

        eventDoc.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()){
                Log.e("CommentsActivity", "Failed to fetch event from database");
            }
            DocumentSnapshot eventDoc = task.getResult();
            if (!eventDoc.exists()){
                Log.e("CommentsActivity", "Event does not exist");
            }

            commentsList = (ArrayList<String>) eventDoc.get("comments");
            adapter = new EventCommentArrayAdapter(this, commentsList, globalUser.getRole());

            commentsLV.setAdapter(adapter);
        });

        backBtn.setOnClickListener(v -> finish());
        postCommentBtn.setOnClickListener(v -> postComment());
    }

    /**
     * Handles posting a new comment
     *
     * <p>Fetches the comment information for the new comment and creates the new Map object to add
     * to the database. Adds the new commentID to the ArrayList and prompts the adapter to update
     * the UI.</p>
     *
     * @see EventCommentArrayAdapter
     */
    private void postComment(){
        String text = String.valueOf(commentTextbox.getText());
        String userID = globalUser.getUserId();
        LocalDateTime timestamp = LocalDateTime.now();

        Map<String, Object> newComment = new HashMap<>();
        newComment.put("text", text);
        newComment.put("userID", userID);
        newComment.put("timestamp", FirestoreUtils.localDateTimeToTimestamp(timestamp, ZoneId.systemDefault()));

        commentTextbox.getText().clear();

        Task<DocumentReference> uploadTask = eventUtils.addCommentToEvent(eventId, newComment);

        uploadTask.addOnCompleteListener(task -> {
            if (!task.isSuccessful()){
                Log.e("CommentActivity", "Failed to post comment");
            }
            DocumentReference commentDoc = task.getResult();

            commentsList.add(commentDoc.getId());
            adapter.notifyDataSetChanged();
        });
    }
}
