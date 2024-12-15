package com.example.foodisea.adapter.repartidor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodisea.R;
import com.example.foodisea.model.Producto;
import com.example.foodisea.model.ProductoCantidad;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;
import java.util.Map;

public class ProductosPedidoAdapter extends RecyclerView.Adapter<ProductosPedidoAdapter.ProductoViewHolder> {

    private final List<ProductoCantidad> productosCantidad;
    private final Map<String, Producto> productosDetalles;

    public ProductosPedidoAdapter(List<ProductoCantidad> productosCantidad, Map<String, Producto> productosDetalles) {
        this.productosCantidad = productosCantidad;
        this.productosDetalles = productosDetalles;
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_producto_pedido, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        ProductoCantidad productoCantidad = productosCantidad.get(position);
        Producto producto = productosDetalles.get(productoCantidad.getProductoId());

        if (producto != null) {
            holder.bind(producto, productoCantidad.getCantidad());
        }
    }

    @Override
    public int getItemCount() {
        return productosCantidad.size();
    }

    static class ProductoViewHolder extends RecyclerView.ViewHolder {
        private final ShapeableImageView ivProductImage;
        private final TextView tvProductName;
        private final TextView tvQuantity;
        private final TextView tvPrice;

        ProductoViewHolder(View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }

        void bind(Producto producto, int cantidad) {
            tvProductName.setText(producto.getNombre());
            tvQuantity.setText(String.format("x%d", cantidad));
            double precioTotal = producto.getPrecio() * cantidad;
            tvPrice.setText(String.format("S/. %.2f", precioTotal));

            // Cargar imagen si existe
            if (producto.getImagenes() != null && !producto.getImagenes().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(producto.getImagenes().get(0))
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .into(ivProductImage);
            }
        }
    }
}