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
import com.example.foodisea.activity.cliente.ClienteRestauranteActivity;
import com.example.foodisea.activity.repartidor.RepartidorRestauranteActivity;
import com.example.foodisea.adapter.RestauranteAdapter;
import com.example.foodisea.entity.Restaurante;

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
        Restaurante restaurant = restaurantes.get(position);

        holder.tvNombre.setText(restaurant.getName());
        holder.tvDireccion.setText(restaurant.getLocation());
        holder.ivImagen.setImageResource(restaurant.getImageResource());


        // Añadir el onClickListener para abrir la actividad de detalles
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, RepartidorRestauranteActivity.class);
            intent.putExtra("name", restaurant.getName());
            intent.putExtra("rating", restaurant.getRating());
            intent.putExtra("image", restaurant.getImageResource());
            intent.putExtra("location", restaurant.getLocation());
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
