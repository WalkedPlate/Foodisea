package com.example.foodisea.adapter.cliente;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.example.foodisea.R;
import com.example.foodisea.activity.cliente.ClienteMainActivity;
import com.example.foodisea.activity.cliente.ClienteRestauranteActivity;
import com.example.foodisea.activity.superadmin.SuperAdminGestionRestauranteActivity;
import com.example.foodisea.activity.superadmin.SuperAdminDetalleRestauranteActivity;
import com.example.foodisea.databinding.ItemRestaurantBinding;
import com.example.foodisea.model.Restaurante;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import java.util.ArrayList; // Import necesario para la lista filtrada

public class RestauranteAdapter extends RecyclerView.Adapter<RestauranteAdapter.RestaurantViewHolder> {

    private List<Restaurante> restaurantes; // Lista original
    private List<Restaurante> restaurantesFiltrados; // Lista filtrada
    private Context context;
    private FirebaseStorage storage;

    public RestauranteAdapter(Context context, List<Restaurante> restaurantes) {
        this.context = context;
        this.restaurantes = restaurantes;
        this.restaurantesFiltrados = new ArrayList<>(restaurantes); // Inicializar con la lista original
        this.storage = FirebaseStorage.getInstance();
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
        Restaurante restaurante = restaurantesFiltrados.get(position); // Usar la lista filtrada

        holder.binding.restaurantName.setText(restaurante.getNombre());

        // Convertir la lista de categorías en un String concatenado
        String categorias = String.join(" - ", restaurante.getCategorias());
        holder.binding.restCategories.setText(categorias);

        // Cargar la imagen desde Firebase Storage
        if (!restaurante.getImagenes().isEmpty()) {
            String imageUrl = restaurante.getImagenes().get(0);

            if (imageUrl.startsWith("https://")) {
                loadImageWithGlide(imageUrl, holder.binding.restaurantImage);
            } else {
                StorageReference imageRef = storage.getReference().child(imageUrl);
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    loadImageWithGlide(uri.toString(), holder.binding.restaurantImage);
                }).addOnFailureListener(e -> {
                    holder.binding.restaurantImage.setImageResource(R.drawable.restaurant_image);
                });
            }
        } else {
            holder.binding.restaurantImage.setImageResource(R.drawable.restaurant_image);
        }

        // Configurar el onClickListener
        holder.itemView.setOnClickListener(v -> {
            Intent intent;
            if (context instanceof ClienteMainActivity) {
                intent = new Intent(context, ClienteRestauranteActivity.class);
            } else if (context instanceof SuperAdminGestionRestauranteActivity) {
                intent = new Intent(context, SuperAdminDetalleRestauranteActivity.class);
            } else {
                return;
            }

            intent.putExtra("restauranteId", restaurante.getId());
            intent.putExtra("name", restaurante.getNombre());
            intent.putExtra("rating", restaurante.getRating());
            intent.putExtra("descripcion", restaurante.getDescripcion());
            if (!restaurante.getImagenes().isEmpty()) {
                intent.putExtra("image", restaurante.getImagenes().get(0));
            }
            intent.putExtra("administradorId", restaurante.getAdministradorId());
            intent.putExtra("direccion", restaurante.getDireccion());
            intent.putExtra("telefono", restaurante.getTelefono());
            context.startActivity(intent);
        });
    }

    private void loadImageWithGlide(String imageUrl, ImageView imageView) {
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.restaurant_image)
                .error(R.drawable.restaurant_image)
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        Glide.with(context)
                .load(imageUrl)
                .apply(requestOptions)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView);
    }

    @Override
    public int getItemCount() {
        return restaurantesFiltrados.size(); // Usar la lista filtrada
    }

    // Método para filtrar la lista
    public void filter(String query) {
        restaurantesFiltrados.clear();
        if (query.isEmpty()) {
            restaurantesFiltrados.addAll(restaurantes); // Mostrar todos si el texto está vacío
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Restaurante restaurante : restaurantes) {
                if (restaurante.getNombre().toLowerCase().contains(lowerCaseQuery)) {
                    restaurantesFiltrados.add(restaurante);
                }
            }
        }
        notifyDataSetChanged(); // Notificar cambios al adaptador
    }

    public static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        ItemRestaurantBinding binding;

        public RestaurantViewHolder(ItemRestaurantBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

