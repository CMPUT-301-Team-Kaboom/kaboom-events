package com.example.projecteventlotteryapp;

import android.net.Uri;

public class Image {
    // imageUrl is set to Uri for ui testing
    // TODO: change back to string when connecting to database
     private String imageId;
     private String imageUrl;
    public Image(String imageId, String imageUrl) {
        this.imageUrl = imageUrl;
        this.imageId = imageId;
    }

    public String getImageId(){ return imageId; }
    public String getImageUrl() {return imageUrl; }
}
