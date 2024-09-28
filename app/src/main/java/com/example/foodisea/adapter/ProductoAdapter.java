package com.example.foodisea.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodisea.R;
import com.example.foodisea.activity.cliente.ClienteProductoActivity;
import com.example.foodisea.databinding.ItemProductoBinding;
import com.example.foodisea.entity.Producto;

import java.util.List;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductViewHolder> {

    private Context context;
    private List<Producto> productList;

    // Constructor
    public ProductoAdapter(Context context, List<Producto> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater =  LayoutInflater.from(parent.getContext());
        ItemProductoBinding binding = ItemProductoBinding.inflate(inflater, parent, false);
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Producto producto = productList.get(position);

        holder.binding.productName.setText(producto.getName());
        holder.binding.productPrice.setText(context.getString(R.string.priceProduct, producto.getPrice()));
        holder.binding.productImage.setImageResource(producto.getImageResource());

        // Mostrar "Agotado" si el producto no está disponible
//        if (producto.isOutOfStock()) {
//            holder.binding.productStatus.setVisibility(View.VISIBLE);
//        } else {
//            holder.binding.productStatus.setVisibility(View.GONE);
//        }

        // Configurar click listener para redirigir a la actividad de detalles del producto
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ClienteProductoActivity.class);
            intent.putExtra("productName", producto.getName());
            intent.putExtra("productPrice", producto.getPrice());
            intent.putExtra("productImage", producto.getImageResource());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {

        ItemProductoBinding binding;

        public ProductViewHolder(ItemProductoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
