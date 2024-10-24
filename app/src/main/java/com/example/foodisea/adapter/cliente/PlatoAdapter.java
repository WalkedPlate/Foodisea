package com.example.foodisea.adapter.cliente;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodisea.R;
import com.example.foodisea.activity.cliente.ClienteProductoActivity;
import com.example.foodisea.databinding.ItemProductoBinding;
import com.example.foodisea.model.Producto;

import java.util.List;

public class PlatoAdapter extends RecyclerView.Adapter<PlatoAdapter.PlatoViewHolder> {

    private Context context;
    private List<Producto> productoList;

    // Constructor
    public PlatoAdapter(Context context, List<Producto> productoList) {
        this.context = context;
        this.productoList = productoList;
    }

    @NonNull
    @Override
    public PlatoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemProductoBinding binding = ItemProductoBinding.inflate(inflater, parent, false);
        return new PlatoViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PlatoViewHolder holder, int position) {
        Producto producto = productoList.get(position);

        holder.binding.productName.setText(producto.getNombre());
        holder.binding.productPrice.setText(context.getString(R.string.priceProduct, producto.getPrecio()));

        // Suponiendo que tienes una lista de imágenes locales en formato de recursos
        if (producto.getImagenes() != null && !producto.getImagenes().isEmpty()) {
            // Cargar la primera imagen de la lista de imágenes
            holder.binding.productImage.setImageResource(context.getResources().getIdentifier(producto.getImagenes().get(0), "drawable", context.getPackageName()));
        }

        // Mostrar "Agotado" si el producto no está disponible
        /*
        if (plato.isOutOfStock()) {
            holder.binding.productStatus.setVisibility(View.VISIBLE);
            holder.binding.productStatus.setText("Agotado");
        } else {
            holder.binding.productStatus.setVisibility(View.GONE);
        }

         */

        // Configurar click listener para redirigir a la actividad de detalles del producto
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ClienteProductoActivity.class);
            intent.putExtra("productName", producto.getNombre());
            intent.putExtra("productPrice", producto.getPrecio());
            intent.putExtra("productDescription", producto.getDescripcion());
            // Se puede agregar un campo para la imagen si se necesita en la actividad de detalles
            intent.putExtra("productImage", producto.getImagenes().get(0)); // Obtener la primera imagen como ejemplo
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return productoList.size();
    }

    public static class PlatoViewHolder extends RecyclerView.ViewHolder {

        ItemProductoBinding binding;

        public PlatoViewHolder(ItemProductoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
