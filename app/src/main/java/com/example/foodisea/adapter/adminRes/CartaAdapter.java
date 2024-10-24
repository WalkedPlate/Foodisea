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

import com.example.foodisea.R;
import com.example.foodisea.activity.adminRes.AdminResDetallesProductoActivity;
import com.example.foodisea.model.Producto;

import java.util.List;

public class CartaAdapter extends RecyclerView.Adapter<CartaAdapter.PlatoViewHolder> {
    private List<Producto> listaProductos; // Cambiar a Plato
    private Context context;

    public List<Producto> getListaPlatos() {
        return listaProductos;
    }

    public void setListaPlatos(List<Producto> listaProductos) {
        this.listaProductos = listaProductos;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public PlatoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_res_carta, parent, false);
        return new PlatoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlatoViewHolder holder, int position) {
        Producto producto = listaProductos.get(position); // Usar Plato en lugar de Producto
        holder.producto = producto;

        TextView textViewNombreProducto = holder.itemView.findViewById(R.id.nameProducto);
        textViewNombreProducto.setText(producto.getNombre()); // Usar getNombre()

        TextView textViewPrecio = holder.itemView.findViewById(R.id.precioProducto);
        textViewPrecio.setText("$" + producto.getPrecio()); // Usar getPrecio()

        ImageView imageViewProducto = holder.itemView.findViewById(R.id.platoImage);
        // Aquí debes cargar la imagen de una URL o recurso, dependiendo de tu implementación
        // Por ejemplo, usando Glide o Picasso si las imágenes están en URLs
        // Glide.with(context).load(plato.getImagenes().get(0)).into(imageViewProducto);
        // Para un recurso local, puedes usar:
        imageViewProducto.setImageResource(context.getResources().getIdentifier(producto.getImagenes().get(0), "drawable", context.getPackageName())); // Asumiendo que la imagen está en una lista de recursos



        // Mostrar "Agotado" si el plato no está disponible
        TextView textViewStatus = holder.itemView.findViewById(R.id.productStatus);
        if (producto.isOutOfStock()) {
            textViewStatus.setText("Agotado");
            textViewStatus.setVisibility(View.VISIBLE);
        } else {
            textViewStatus.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AdminResDetallesProductoActivity.class);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listaProductos.size(); // Cambiar a listaPlatos
    }

    public class PlatoViewHolder extends RecyclerView.ViewHolder {
        Producto producto; // Cambiar Producto a Plato

        public PlatoViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
