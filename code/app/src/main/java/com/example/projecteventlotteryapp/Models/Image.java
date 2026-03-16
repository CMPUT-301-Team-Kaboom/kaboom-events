package com.example.projecteventlotteryapp.Models;

/**
 * Represents an image in the app.
 *
 * Carries the ID of the image, which is the document ID of the image in the database, and the
 * download url of the image from storage
 *
 * @author Ashley Kang
 */
public class Image {
     private String imageId;
     private String imageUrl;
    public Image(String imageId, String imageUrl) {
        this.imageUrl = imageUrl;
        this.imageId = imageId;
    }

    /**
     * Retrieves the Image ID.
     * @return the ID of the image
     */
    public String getImageId(){ return imageId; }

    /**
     * Retrieves the Image URL.
     * @return the URL of the image
     */
    public String getImageUrl() {return imageUrl; }
}
