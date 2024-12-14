package com.example.foodisea.adapter.adminRes;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodisea.R;
import com.example.foodisea.activity.adminRes.AdminResDetallesProductoActivity;
import com.example.foodisea.model.Producto;

import java.util.ArrayList;
import java.util.List;

public class CartaAdapter extends RecyclerView.Adapter<CartaAdapter.PlatoViewHolder> {
    private List<Producto> listaProductos = new ArrayList<>(); // Inicializar lista vacía
    private Context context;

    @NonNull
    @Override
    public PlatoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_res_carta, parent, false);
        return new PlatoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlatoViewHolder holder, int position) {
        Producto producto = listaProductos.get(position);
        holder.bind(producto);
    }

    @Override
    public int getItemCount() {
        return listaProductos != null ? listaProductos.size() : 0;
    }

    public void setListaPlatos(List<Producto> nuevaLista) {
        this.listaProductos = nuevaLista != null ? nuevaLista : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public class PlatoViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewNombreProducto;
        private final TextView textViewPrecio;
        private final ImageView imageViewProducto;
        private final TextView textViewStatus;

        public PlatoViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNombreProducto = itemView.findViewById(R.id.nameProducto);
            textViewPrecio = itemView.findViewById(R.id.precioProducto);
            imageViewProducto = itemView.findViewById(R.id.platoImage);
            textViewStatus = itemView.findViewById(R.id.productStatus);
        }

        public void bind(Producto producto) {
            textViewNombreProducto.setText(producto.getNombre());
            textViewPrecio.setText(String.format("S/. %.2f", producto.getPrecio()));

            // Cargar imagen con Glide
            if (producto.getImagenes() != null && !producto.getImagenes().isEmpty()) {
                Glide.with(context)
                        .load(producto.getImagenes().get(0))
                        .placeholder(R.drawable.placeholder_image) // Reemplaza con tu placeholder
                        .error(R.drawable.error_image) // Reemplaza con tu imagen de error
                        .centerCrop()
                        .into(imageViewProducto);
            } else {
                // Si no hay imágenes, mostrar placeholder
                imageViewProducto.setImageResource(R.drawable.placeholder_image);
            }

            // Mostrar estado de disponibilidad
            textViewStatus.setVisibility(producto.isOutOfStock() ? View.VISIBLE : View.GONE);
            textViewStatus.setText(producto.isOutOfStock() ? "Agotado" : "");

            // Click listener para ver detalles
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, AdminResDetallesProductoActivity.class);
                // Pasar el ID del producto
                intent.putExtra("PRODUCTO_ID", producto.getId());
                context.startActivity(intent);
            });
        }
    }
}
