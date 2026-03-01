package com.example.projecteventlotteryapp;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class OrganizerEventDetailsActivity extends AppCompatActivity {
    private Button btnEditPoster;
    private ActivityResultLauncher<String> posterImageLauncher;
    private Uri eventPosterFilepath;
    private ImageView imgEventPoster;
    private PosterImageHandler posterImageHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_organizer_event_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnEditPoster = findViewById(R.id.btn_organizer_event_edit);
        imgEventPoster = findViewById(R.id.img_organizer_details_poster);
        posterImageHandler = new PosterImageHandler();

        posterImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null){
                        eventPosterFilepath = uri;
                        try {
                            imgEventPoster.setImageURI(uri);
                            // posterImageHandler.uploadPoster("example123", this, eventPosterFilepath);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Failed to upload image!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        btnEditPoster.setOnClickListener(v -> posterImageLauncher.launch("image/*"));
    }
}