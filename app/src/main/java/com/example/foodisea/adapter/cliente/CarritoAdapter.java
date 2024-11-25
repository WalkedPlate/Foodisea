package com.example.foodisea.adapter.cliente;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodisea.R;
import com.example.foodisea.model.Producto;
import com.example.foodisea.model.ProductoCantidad;


import java.util.List;
import java.util.Locale;


public class CarritoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_CARRITO = 0;
    private static final int VIEW_TYPE_CHECKOUT = 1;

    private List<CarritoItem> cartItems; // Clase auxiliar que combina ProductoCantidad y Producto
    private Context context;
    private boolean isCheckout;
    private CarritoItemListener listener;

    // Interfaz para manejar eventos
    public interface CarritoItemListener {
        void onQuantityChanged(String productoId, int newQuantity);
        void onDeleteItem(String productoId);
    }

    public static class CarritoItem {
        private ProductoCantidad productoCantidad;
        private Producto producto;

        public CarritoItem(ProductoCantidad pc, Producto p) {
            this.productoCantidad = pc;
            this.producto = p;
        }

        public ProductoCantidad getProductoCantidad() {
            return productoCantidad;
        }

        public void setProductoCantidad(ProductoCantidad productoCantidad) {
            this.productoCantidad = productoCantidad;
        }

        public Producto getProducto() {
            return producto;
        }

        public void setProducto(Producto producto) {
            this.producto = producto;
        }
    }

    public CarritoAdapter(Context context, List<CarritoItem> cartItems, boolean isCheckout,
                          CarritoItemListener listener) {
        this.context = context;
        this.cartItems = cartItems;
        this.isCheckout = isCheckout;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return isCheckout ? VIEW_TYPE_CHECKOUT : VIEW_TYPE_CARRITO;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_CARRITO) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_producto_carrito, parent, false);
            return new CartViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_product_checkout, parent, false);
            return new CheckoutViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        CarritoItem item = cartItems.get(position);
        Producto producto = item.producto;
        ProductoCantidad pc = item.productoCantidad;

        if (holder instanceof CartViewHolder) {
            CartViewHolder cartViewHolder = (CartViewHolder) holder;
            cartViewHolder.bind(producto, pc);
        } else if (holder instanceof CheckoutViewHolder) {
            CheckoutViewHolder checkoutViewHolder = (CheckoutViewHolder) holder;
            checkoutViewHolder.bind(producto, pc);
        }
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public void updateItems(List<CarritoItem> newItems) {
        this.cartItems = newItems;
        notifyDataSetChanged();
    }

    public class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productPrice, productQuantity;
        Button btnMinus, btnPlus, btnDelete;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productQuantity = itemView.findViewById(R.id.tvQuantity);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void bind(Producto producto, ProductoCantidad pc) {
            productName.setText(producto.getNombre());
            productPrice.setText(String.format(Locale.getDefault(), "S/. %.2f", producto.getPrecio()));
            productQuantity.setText(String.valueOf(pc.getCantidad()));

            // Cargar la primera imagen del producto
            if (!producto.getImagenes().isEmpty()) {
                Glide.with(context)
                        .load(producto.getImagenes().get(0))
                        .placeholder(R.drawable.placeholder_image)
                        .into(productImage);
            }

            // Configurar listeners
            btnMinus.setOnClickListener(v -> {
                int newQuantity = pc.getCantidad() - 1;
                if (newQuantity >= 0 && listener != null) {
                    listener.onQuantityChanged(pc.getProductoId(), newQuantity);
                }
            });

            btnPlus.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onQuantityChanged(pc.getProductoId(), pc.getCantidad() + 1);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteItem(pc.getProductoId());
                }
            });
        }
    }

    public class CheckoutViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice, productQuantity;
        ImageView productImage;

        public CheckoutViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productQuantity = itemView.findViewById(R.id.productQuantity1);
            productImage = itemView.findViewById(R.id.productImage);
        }

        void bind(Producto producto, ProductoCantidad pc) {
            productName.setText(producto.getNombre());
            productPrice.setText(String.format(Locale.getDefault(), "S/. %.2f", producto.getPrecio()));
            productQuantity.setText(String.valueOf(pc.getCantidad()));

            // Cargar la primera imagen
            if (!producto.getImagenes().isEmpty()) {
                Glide.with(context)
                        .load(producto.getImagenes().get(0))
                        .placeholder(R.drawable.placeholder_image)
                        .into(productImage);
            }
        }
    }
}