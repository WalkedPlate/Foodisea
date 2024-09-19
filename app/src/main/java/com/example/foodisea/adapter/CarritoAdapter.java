package com.example.foodisea.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodisea.R;
import com.example.foodisea.entity.ProductoCarrito;

import java.util.List;

public class CarritoAdapter extends RecyclerView.Adapter<CarritoAdapter.CartViewHolder> {

    private List<ProductoCarrito> cartItemList;

    // Constructor del adaptador
    public CarritoAdapter(List<ProductoCarrito> cartItemList) {
        this.cartItemList = cartItemList;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_producto_carrito, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        ProductoCarrito cartItem = cartItemList.get(position);

        holder.productName.setText(cartItem.getProductName());
        holder.productPrice.setText("S/. " + cartItem.getProductPrice());
        holder.productQuantity.setText(String.valueOf(cartItem.getQuantity()));
        holder.productImage.setImageResource(cartItem.getImageResource());
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {

        ImageView productImage;
        TextView productName, productSize, productPrice, productQuantity;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productQuantity = itemView.findViewById(R.id.tvQuantity);
        }
    }
}
