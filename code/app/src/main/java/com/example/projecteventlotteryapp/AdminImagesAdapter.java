package com.example.projecteventlotteryapp;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AdminImagesAdapter extends RecyclerView.Adapter<AdminImagesAdapter.ViewHolder>{
    /*
    the following code is adapted from https://www.geeksforgeeks.org/android/how-to-build-an-image-gallery-android-app-with-recyclerview-and-glide/
     */
    private Context context;
    ArrayList<Image> imageList;
    public AdminImagesAdapter(Context context, ArrayList<Image> imageList){
        this.context = context;
        this.imageList = imageList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.admin_image_item, null, true);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Image model = imageList.get(position);
        holder.image.setImageResource(R.drawable.default_poster);
        holder.deleteBtn.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();

            if (pos != RecyclerView.NO_POSITION) {
                Image image = imageList.get(pos);
                deleteImage(image, pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    private void deleteImage(Image image, int position){
        imageList.remove(position);
        notifyItemRemoved(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        ImageButton deleteBtn;
        public ViewHolder(@NonNull View itemView){
            super(itemView);
            image = itemView.findViewById(R.id.iv_admin_images_item);
            deleteBtn = itemView.findViewById(R.id.btn_admin_images_delete);
        }
    }
}
