package com.example.projecteventlotteryapp;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AdminImagesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AdminImagesAdapter adapter;
    private GridLayoutManager manager;
    private ArrayList<Image> imageList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_images);

        recyclerView = findViewById(R.id.admin_recycler);
        adapter = new AdminImagesAdapter(this,imageList);
        manager = new GridLayoutManager(this, 2);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(manager);
    }
}
