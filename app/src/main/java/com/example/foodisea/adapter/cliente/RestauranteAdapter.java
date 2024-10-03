package com.example.foodisea.adapter.cliente;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodisea.R;
import com.example.foodisea.activity.cliente.ClienteRestauranteActivity;
import com.example.foodisea.databinding.ItemRestaurantBinding;
import com.example.foodisea.model.Restaurante;

import java.util.List;

public class RestauranteAdapter extends RecyclerView.Adapter<RestauranteAdapter.RestaurantViewHolder> {

    private List<Restaurante> restaurantes;
    private Context context;

    public RestauranteAdapter(Context context, List<Restaurante> restaurantes) {
        this.context = context;
        this.restaurantes = restaurantes;
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemRestaurantBinding binding = ItemRestaurantBinding.inflate(inflater, parent, false);
        return new RestaurantViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        Restaurante restaurante = restaurantes.get(position);

        holder.binding.restaurantName.setText(restaurante.getNombre());

        // Convertir la lista de categorías en un String concatenado
        String categorias = String.join(" - ", restaurante.getCategorias());
        holder.binding.restCategories.setText(categorias);

        // Mostrar la calificación del restaurante
        holder.binding.restaurantRating.setText(String.valueOf(restaurante.getRating()));

        // Cargar la primera imagen del restaurante desde recursos locales
        holder.binding.restaurantImage.setImageResource(R.drawable.restaurant_image); // Imagen por defecto
        if (!restaurante.getImagenes().isEmpty()) {
            // Asumiendo que tienes un recurso con nombre que coincide con la primera imagen de la lista
            int imageResId = context.getResources().getIdentifier(restaurante.getImagenes().get(0), "drawable", context.getPackageName());
            if (imageResId != 0) {
                holder.binding.restaurantImage.setImageResource(imageResId);
            } else {
                holder.binding.restaurantImage.setImageResource(R.drawable.restaurant_image); // Imagen por defecto si no se encuentra
            }
        }

        // Añadir el onClickListener para abrir la actividad de detalles
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ClienteRestauranteActivity.class);
            intent.putExtra("restauranteId", restaurante.getId());  // Pasar el ID del restaurante
            intent.putExtra("name", restaurante.getNombre());
            intent.putExtra("rating", restaurante.getRating());
            intent.putExtra("descripcion", restaurante.getDescripcion());
            if (!restaurante.getImagenes().isEmpty()) {
                intent.putExtra("image", restaurante.getImagenes().get(0));  // Pasar la primera imagen
            }
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return restaurantes.size();
    }

    public static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        ItemRestaurantBinding binding;

        public RestaurantViewHolder(ItemRestaurantBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
