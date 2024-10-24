package com.example.foodisea.adapter.adminRes;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodisea.R;

import java.util.ArrayList;
import java.util.List;


public class ImagePreviewAdapter extends RecyclerView.Adapter<ImagePreviewAdapter.ImageViewHolder> {
    private List<Uri> localImages;
    private List<String> existingImageUrls;
    private OnImageDeleteListener deleteListener;

    public interface OnImageDeleteListener {
        void onLocalImageDelete(Uri uri);
        void onExistingImageDelete(String url);
    }

    public ImagePreviewAdapter(List<Uri> localImages, List<String> existingImageUrls, OnImageDeleteListener deleteListener) {
        this.localImages = localImages != null ? localImages : new ArrayList<>();
        this.existingImageUrls = existingImageUrls != null ? existingImageUrls : new ArrayList<>();
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_preview, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        if (position < existingImageUrls.size()) {
            // Cargar imagen existente
            String imageUrl = existingImageUrls.get(position);
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .centerCrop()
                    .into(holder.imageView);

            holder.deleteButton.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onExistingImageDelete(imageUrl);
                }
            });
        } else {
            // Cargar imagen local nueva
            Uri imageUri = localImages.get(position - existingImageUrls.size());
            Glide.with(holder.itemView.getContext())
                    .load(imageUri)
                    .centerCrop()
                    .into(holder.imageView);

            holder.deleteButton.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onLocalImageDelete(imageUri);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return localImages.size() + existingImageUrls.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton deleteButton;

        ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imagePreview);
            deleteButton = itemView.findViewById(R.id.btnDelete);
        }
    }
}
