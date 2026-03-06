package com.example.projecteventlotteryapp;

import android.net.Uri;

public class Image {
    // imageUrl is set to Uri for ui testing
    // TODO: change back to string when connecting to database
    private Uri imageUrl;
    public Image(Uri imageUrl){
        this.imageUrl = imageUrl;
    }
    public void setImageUrl(Uri imageUrl) {
        this.imageUrl = imageUrl;
    }
    public Uri getImageUrl() {
        return imageUrl;
    }
}
