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
import com.example.foodisea.model.Plato;

import java.util.List;

public class PlatoAdapter extends RecyclerView.Adapter<PlatoAdapter.PlatoViewHolder> {

    private Context context;
    private List<Plato> platoList;

    // Constructor
    public PlatoAdapter(Context context, List<Plato> platoList) {
        this.context = context;
        this.platoList = platoList;
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
        Plato plato = platoList.get(position);

        holder.binding.productName.setText(plato.getNombre());
        holder.binding.productPrice.setText(context.getString(R.string.priceProduct, plato.getPrecio()));

        // Suponiendo que tienes una lista de imágenes locales en formato de recursos
        if (plato.getImagenes() != null && !plato.getImagenes().isEmpty()) {
            // Cargar la primera imagen de la lista de imágenes
            holder.binding.productImage.setImageResource(context.getResources().getIdentifier(plato.getImagenes().get(0), "drawable", context.getPackageName()));
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
            intent.putExtra("productName", plato.getNombre());
            intent.putExtra("productPrice", plato.getPrecio());
            intent.putExtra("productDescription", plato.getDescripcion());
            // Se puede agregar un campo para la imagen si se necesita en la actividad de detalles
            intent.putExtra("productImage", plato.getImagenes().get(0)); // Obtener la primera imagen como ejemplo
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return platoList.size();
    }

    public static class PlatoViewHolder extends RecyclerView.ViewHolder {

        ItemProductoBinding binding;

        public PlatoViewHolder(ItemProductoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
