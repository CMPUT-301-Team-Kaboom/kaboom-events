package com.example.projecteventlotteryapp;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/*
Displays a grid of all posters in the app and allows admins to delete posters
Admins cannot delete the default poster

Author: Ashley Kang
 */
public class AdminImagesActivity extends AppCompatActivity {
    /*
    the following code is adapted from https://www.geeksforgeeks.org/android/how-to-build-an-image-gallery-android-app-with-recyclerview-and-glide/
     */
    private RecyclerView recyclerView;
    private AdminImagesAdapter adapter;
    private GridLayoutManager manager;
    private ArrayList<String> imageList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_images);

        imageList = new ArrayList<>();
        recyclerView = findViewById(R.id.rv_admin_images_list);
        adapter = new AdminImagesAdapter(this,imageList);
        manager = new GridLayoutManager(this, 2);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(manager);

        PosterImageHandler.getAllPosters(posters -> {
            imageList.clear();
            imageList.addAll(posters);

            adapter.notifyDataSetChanged();
        });

        ImageButton backButton = findViewById(R.id.btn_admin_images_back);
        backButton.setOnClickListener(v -> finish());

    }
}
