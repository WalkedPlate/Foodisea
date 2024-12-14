package com.example.foodisea.adapter.cliente;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.example.foodisea.R;
import com.example.foodisea.item.OrderItem;
import com.google.android.material.imageview.ShapeableImageView;


import java.util.List;

public class DetallePedidoAdapter extends RecyclerView.Adapter<DetallePedidoAdapter.ViewHolder> {
    private List<OrderItem> orderItems;
    private Context context;

    public DetallePedidoAdapter(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView itemImage;
        TextView itemName;
        TextView itemQuantity;
        TextView productPrice;

        public ViewHolder(View view) {
            super(view);
            itemImage = view.findViewById(R.id.ivItemImage);
            itemName = view.findViewById(R.id.tvItemName);
            itemQuantity = view.findViewById(R.id.tvItemQuantity);
            productPrice = view.findViewById(R.id.productPrice);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderItem orderItem = orderItems.get(position);

        holder.itemName.setText(orderItem.getName());
        holder.itemQuantity.setText(String.valueOf(orderItem.getQuantity()));
        holder.productPrice.setText(String.format("S/. %.2f", orderItem.getPrice()));

        // Cargar imagen usando Glide
        if (orderItem.getImageUrl() != null && !orderItem.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(orderItem.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(holder.itemImage);
        } else {
            holder.itemImage.setImageResource(R.drawable.placeholder_image);
        }
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }

    public void setItems(List<OrderItem> items) {
        this.orderItems = items;
        notifyDataSetChanged();
    }
}