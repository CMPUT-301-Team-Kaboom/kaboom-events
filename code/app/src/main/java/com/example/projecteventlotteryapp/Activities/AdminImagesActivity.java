package com.example.projecteventlotteryapp.Activities;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projecteventlotteryapp.AdminImagesAdapter;
import com.example.projecteventlotteryapp.Models.Image;
import com.example.projecteventlotteryapp.PosterImageHandler;
import com.example.projecteventlotteryapp.R;

import java.util.ArrayList;

/*
Displays a grid of all posters in the app and allows admins to delete posters
Admins cannot delete the default poster

Author: Ashley Kang
 */
public class AdminImagesActivity extends BaseActivity {
    /*
    the following code is adapted from https://www.geeksforgeeks.org/android/how-to-build-an-image-gallery-android-app-with-recyclerview-and-glide/
     */
    private RecyclerView recyclerView;
    private AdminImagesAdapter adapter;
    private GridLayoutManager manager;
    private ArrayList<Image> imageList;
    private PosterImageHandler posterImageHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_images);

        imageList = new ArrayList<>();
        posterImageHandler = new PosterImageHandler();
        recyclerView = findViewById(R.id.rv_admin_images_list);
        adapter = new AdminImagesAdapter(this,imageList);
        manager = new GridLayoutManager(this, 2);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(manager);

        posterImageHandler.getAllPosters(posters -> {
            imageList.clear();
            imageList.addAll(posters);

            adapter.notifyDataSetChanged();
        });

        ImageButton backButton = findViewById(R.id.btn_admin_images_back);
        backButton.setOnClickListener(v -> finish());

    }
}
