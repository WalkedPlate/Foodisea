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
import com.example.foodisea.activity.cliente.ClienteRestauranteActivity;
import com.example.foodisea.entity.Restaurante;

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_restaurant, parent, false);
        return new RestaurantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        Restaurante restaurant = restaurantes.get(position);

        holder.restaurantName.setText(restaurant.getName());
        holder.restaurantPlates.setText(restaurant.getPlates());
        holder.restaurantRating.setText(String.valueOf(restaurant.getRating()));
        holder.restaurantImage.setImageResource(restaurant.getImageResource());


        // Añadir el onClickListener para abrir la actividad de detalles
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ClienteRestauranteActivity.class);
            intent.putExtra("name", restaurant.getName());
            intent.putExtra("rating", restaurant.getRating());
            intent.putExtra("image", restaurant.getImageResource());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return restaurantes.size();
    }

    public static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        public ImageView restaurantImage;
        public TextView restaurantName;
        public TextView restaurantPlates;
        public TextView restaurantRating;

        public RestaurantViewHolder(@NonNull View itemView) {
            super(itemView);
            restaurantImage = itemView.findViewById(R.id.restaurantImage);
            restaurantName = itemView.findViewById(R.id.restaurantName);
            restaurantPlates = itemView.findViewById(R.id.restaurantPlates);
            restaurantRating = itemView.findViewById(R.id.restaurantRating);
        }
    }


}