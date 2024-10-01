package com.example.foodisea.adapter.repartidor;

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
import com.example.foodisea.activity.repartidor.RepartidorRestauranteActivity;
import com.example.foodisea.model.Restaurante;

import java.util.List;

public class RestaurantRepartidorAdapter extends RecyclerView.Adapter<RestaurantRepartidorAdapter.RestauranteViewHolder> {

    private List<Restaurante> restaurantes;
    private Context context;

    public RestaurantRepartidorAdapter(Context context, List<Restaurante> restaurantes) {
        this.context = context;
        this.restaurantes = restaurantes;
    }

    @NonNull
    @Override
    public RestaurantRepartidorAdapter.RestauranteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_restaurant_repartidor_main, parent, false);
        return new RestaurantRepartidorAdapter.RestauranteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantRepartidorAdapter.RestauranteViewHolder holder, int position) {
        Restaurante restaurante = restaurantes.get(position);

        holder.tvNombre.setText(restaurante.getNombre());
        holder.tvDireccion.setText(restaurante.getDireccion());

        // Cargar la primera imagen de la lista de imágenes
        if (!restaurante.getImagenes().isEmpty()) {
            int imageResId = context.getResources().getIdentifier(restaurante.getImagenes().get(0), "drawable", context.getPackageName());
            if (imageResId != 0) {
                holder.ivImagen.setImageResource(imageResId);
            } else {
                holder.ivImagen.setImageResource(R.drawable.restaurant_image); // Imagen por defecto si no se encuentra
            }
        } else {
            holder.ivImagen.setImageResource(R.drawable.restaurant_image); // Imagen por defecto si no hay imágenes
        }

        // Añadir el onClickListener para abrir la actividad de detalles
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, RepartidorRestauranteActivity.class);
            intent.putExtra("name", restaurante.getNombre());
            intent.putExtra("rating", restaurante.getRating());
            intent.putExtra("image", restaurante.getImagenes().isEmpty() ? null : restaurante.getImagenes().get(0)); // Pasar la primera imagen
            intent.putExtra("location", restaurante.getDireccion());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return restaurantes.size();
    }

    public static class RestauranteViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvDireccion;
        ImageView ivImagen;

        public RestauranteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreRestaurante);
            tvDireccion = itemView.findViewById(R.id.tvDireccionRestaurante);
            ivImagen = itemView.findViewById(R.id.ivImagenRestaurante);
        }
    }
}
