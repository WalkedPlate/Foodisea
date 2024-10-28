package com.example.foodisea.adapter.repartidor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodisea.R;
import com.example.foodisea.item.OrderItem;

import java.util.List;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.ViewHolder> {

    private List<OrderItem> orderItems;

    public OrderItemAdapter(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView itemImage;
        public TextView itemName;
        public TextView itemSize;
        public TextView itemQuantity;

        public ViewHolder(View view) {
            super(view);
            itemImage = view.findViewById(R.id .ivItemImage);
            itemName = view.findViewById(R.id.tvItemName);
            itemSize = view.findViewById(R.id.tvItemSize);
            itemQuantity = view.findViewById(R.id.tvItemQuantity);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderItem item = orderItems.get(position);
        holder.itemImage.setImageResource(item.getImageResId());
        holder.itemName.setText(item.getName());
        holder.itemSize.setText(item.getSize());
        holder.itemQuantity.setText(String.valueOf(item.getQuantity()));
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }
}
