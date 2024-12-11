package com.example.foodisea.adapter.adminRes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodisea.R;
import com.example.foodisea.dto.ProductoClaseCantidad;
import com.example.foodisea.model.Producto;

import java.util.List;

public class ProductoDetalleAdapter extends RecyclerView.Adapter<ProductoDetalleAdapter.ProductoViewHolder> {

    private List<ProductoClaseCantidad> listaProductosCantidad;
    private Context context;

    public List<ProductoClaseCantidad> getListaProductosCantidad() {
        return listaProductosCantidad;
    }

    public void setListaProductosCantidad(List<ProductoClaseCantidad> listaProductosCantidad) {
        this.listaProductosCantidad = listaProductosCantidad;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_checkout,parent,false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        ProductoClaseCantidad p = listaProductosCantidad.get(position);
        holder.productoClaseCantidad = p;

        TextView textViewProductoName = holder.itemView.findViewById(R.id.productName);
        textViewProductoName.setText(p.getProducto().getNombre());

        TextView textViewProductoPrecio = holder.itemView.findViewById(R.id.productPrice);
        textViewProductoPrecio.setText("s/. " + p.getProducto().getPrecio());

        TextView textViewCantidad = holder.itemView.findViewById(R.id.productQuantity1);
        textViewCantidad.setText(String.valueOf(p.getCantidad()));

        ImageView imageViewProducto = holder.itemView.findViewById(R.id.productImage);
        // Cargar imagen con Glide
        if (p.getProducto().getImagenes() != null && !p.getProducto().getImagenes().isEmpty()) {
            Glide.with(context)
                    .load(p.getProducto().getImagenes().get(0))
                    .placeholder(R.drawable.placeholder_image) // Reemplaza con tu placeholder
                    .error(R.drawable.error_image) // Reemplaza con tu imagen de error
                    .centerCrop()
                    .into(imageViewProducto);
        } else {
            // Si no hay imágenes, mostrar placeholder
            imageViewProducto.setImageResource(R.drawable.placeholder_image);
        }
    }

    @Override
    public int getItemCount() {
        return listaProductosCantidad.size();
    }

    public class ProductoViewHolder extends RecyclerView.ViewHolder{

        ProductoClaseCantidad productoClaseCantidad;
        public ProductoViewHolder(@NonNull View itermView) {
            super(itermView);
        }
    }
}
