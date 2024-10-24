package com.example.foodisea.adapter.cliente;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.foodisea.R;
import com.example.foodisea.entity.OrderItem;


import java.util.List;

public class DetallePedidoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<OrderItem> orderItems;


    public DetallePedidoAdapter(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView itemImage;
        public TextView itemName;
        public TextView itemQuantity;

        public ViewHolder(View view) {
            super(view);
            itemImage = view.findViewById(R.id .ivItemImage);
            itemName = view.findViewById(R.id.tvItemName);
            itemQuantity = view.findViewById(R.id.tvItemQuantity);
        }
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        OrderItem orderItem = orderItems.get(position);
        viewHolder.itemName.setText(orderItem.getName());
        viewHolder.itemQuantity.setText(String.valueOf(orderItem.getQuantity()));
        viewHolder.itemImage.setImageResource(orderItem.getImageResId());
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }

}
