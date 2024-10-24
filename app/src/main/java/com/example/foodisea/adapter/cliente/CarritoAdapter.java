package com.example.foodisea.adapter.cliente;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodisea.R;
import com.example.foodisea.model.Producto;
import com.example.foodisea.model.ProductoCantidad;


import java.util.List;
import java.util.Locale;


public class CarritoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_CARRITO = 0;
    private static final int VIEW_TYPE_CHECKOUT = 1;

    private List<ProductoCantidad> cartItemList;
    private Context context; // Añadido contexto
    private boolean isCheckout;

    // Constructor del adaptador
    public CarritoAdapter(Context context, List<ProductoCantidad> cartItemList, boolean isCheckout) {
        this.context = context; // Asignar el contexto
        this.cartItemList = cartItemList;
        this.isCheckout = isCheckout;
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
        ProductoCantidad cartItem = cartItemList.get(position);
        Producto producto = obtenerPlatoPorId(cartItem.getProductoId());

        if (holder instanceof CartViewHolder) {
            CartViewHolder cartViewHolder = (CartViewHolder) holder;
            cartViewHolder.productName.setText(producto.getNombre());
            cartViewHolder.productPrice.setText(String.format(Locale.getDefault(), "S/. %.2f", producto.getPrecio()));
            cartViewHolder.productQuantity.setText(String.valueOf(cartItem.getCantidad()));

            // Cargar la imagen correspondiente
            if (!producto.getImagenes().isEmpty()) {
                int imageResource = getImageResource(producto.getImagenes().get(0));
                cartViewHolder.productImage.setImageResource(imageResource);
            }

        } else if (holder instanceof CheckoutViewHolder) {
            CheckoutViewHolder checkoutViewHolder = (CheckoutViewHolder) holder;
            checkoutViewHolder.productName.setText(producto.getNombre());
            checkoutViewHolder.productPrice.setText(String.format(Locale.getDefault(), "S/. %.2f", producto.getPrecio()));
            checkoutViewHolder.productQuantity1.setText(String.valueOf(cartItem.getCantidad()));

            // Cargar la imagen correspondiente
            if (!producto.getImagenes().isEmpty()) {
                int imageResource = getImageResource(producto.getImagenes().get(0));
                ((ImageView) holder.itemView.findViewById(R.id.productImage)).setImageResource(imageResource);
            }
        }
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {

        ImageView productImage;
        TextView productName, productPrice, productQuantity;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productQuantity = itemView.findViewById(R.id.tvQuantity);
        }
    }


    public static class CheckoutViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice, productQuantity1;

        public CheckoutViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productQuantity1 = itemView.findViewById(R.id.productQuantity1);
        }
    }

    private Producto obtenerPlatoPorId(String platoId) {
        switch (platoId) {
            case "PlatoId1":
                return new Producto(platoId, "Burger Ferguson", "Pizza clásica con tomate y mozzarella", 10.00,
                        List.of("burger"), "Platos", false);
            case "PlatoId2":
                return new Producto(platoId, "Rockin' Burgers", "Hamburguesa con queso cheddar y lechuga", 15.30,
                        List.of("burger2"), "Platos", false);
            case "PlatoId3":
                return new Producto(platoId, "Coca Cola", "Ensalada con pollo, lechuga y salsa César", 5.00,
                        List.of("soda"), "Bebidas", true);
            case "PlatoId4":
                return new Producto(platoId, "Crack' Burgers", "Taco con carne de cerdo y piña", 25.00,
                        List.of("burger2"), "Tacos", false);
            default:
                return new Producto(platoId, "Plato Desconocido", "Descripción no disponible", 0.0,
                        List.of("default_image"), "Desconocido", false);
        }
    }


    // Método para obtener el recurso de imagen (debes implementar esta lógica)
    private int getImageResource(String imagen) {
        // Aquí puedes usar el contexto para obtener el recurso de imagen.
        // Por ejemplo, usando `context.getResources().getIdentifier` para cargar imágenes por nombre
        int resourceId = context.getResources().getIdentifier(imagen, "drawable", context.getPackageName());
        return resourceId != 0 ? resourceId : R.drawable.burger_image; // Retorna una imagen por defecto si no se encuentra
    }
}
