package com.example.foodisea.adapter.cliente;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.example.foodisea.R;
import com.example.foodisea.activity.cliente.ClienteProductoActivity;
import com.example.foodisea.databinding.ItemProductoBinding;
import com.example.foodisea.model.Producto;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class PlatoAdapter extends RecyclerView.Adapter<PlatoAdapter.PlatoViewHolder> {

    private Context context;
    private List<Producto> productoList;
    private FirebaseStorage storage;

    public PlatoAdapter(Context context, List<Producto> productoList) {
        this.context = context;
        this.productoList = productoList;
        this.storage = FirebaseStorage.getInstance();
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

        // Manejar estado de disponibilidad
        /*
        if (producto.isOutOfStock()) {
            holder.binding.productStatus.setVisibility(View.VISIBLE);
            holder.binding.productStatus.setText("Agotado");
        } else {
            holder.binding.productStatus.setVisibility(View.GONE);
        }

         */

        // Cargar imagen desde Firebase Storage
        if (producto.getImagenes() != null && !producto.getImagenes().isEmpty()) {
            String imageRef = producto.getImagenes().get(0);
            if (imageRef.startsWith("https://")) {
                loadImageWithGlide(imageRef, holder.binding.productImage);
            } else {
                StorageReference storageRef = storage.getReference().child(imageRef);
                storageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> loadImageWithGlide(uri.toString(), holder.binding.productImage))
                        .addOnFailureListener(e -> {
                            holder.binding.productImage.setImageResource(R.drawable.placeholder_image);
                        });
            }
        } else {
            holder.binding.productImage.setImageResource(R.drawable.placeholder_image);
        }

        // Click listener para detalles del producto
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ClienteProductoActivity.class);
            intent.putExtra("productoId", producto.getId());
            intent.putExtra("productName", producto.getNombre());
            intent.putExtra("productPrice", producto.getPrecio());
            intent.putExtra("productDescription", producto.getDescripcion());
            intent.putExtra("restauranteId", producto.getRestauranteId());
            context.startActivity(intent);
        });
    }

    private void loadImageWithGlide(String imageUrl, ImageView imageView) {
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        Glide.with(context)
                .load(imageUrl)
                .apply(requestOptions)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView);
    }

    @Override
    public int getItemCount() {
        return productoList.size();
    }

    public void actualizarProductos(List<Producto> nuevaLista) {
        this.productoList = nuevaLista;
        notifyDataSetChanged();
    }

    public static class PlatoViewHolder extends RecyclerView.ViewHolder {
        ItemProductoBinding binding;

        public PlatoViewHolder(ItemProductoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
