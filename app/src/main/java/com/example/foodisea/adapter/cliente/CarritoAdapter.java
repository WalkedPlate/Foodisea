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
import com.example.foodisea.model.Plato;
import com.example.foodisea.model.PlatoCantidad;


import java.util.List;

public class CarritoAdapter extends RecyclerView.Adapter<CarritoAdapter.CartViewHolder> {

    private List<PlatoCantidad> cartItemList;
    private Context context; // Añadido contexto

    // Constructor del adaptador
    public CarritoAdapter(Context context, List<PlatoCantidad> cartItemList) {
        this.context = context; // Asignar el contexto
        this.cartItemList = cartItemList;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_producto_carrito, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        PlatoCantidad cartItem = cartItemList.get(position);

        // Supongamos que tienes una forma de obtener un objeto Plato desde el ID
        Plato plato = obtenerPlatoPorId(cartItem.getPlatoId());

        holder.productName.setText(plato.getNombre()); // Muestra el nombre del plato
        holder.productPrice.setText("S/. " + plato.getPrecio()); // Muestra el precio del plato
        holder.productQuantity.setText(String.valueOf(cartItem.getCantidad())); // Muestra la cantidad
        // Muestra la primera imagen del plato
        if (!plato.getImagenes().isEmpty()) {
            // Cargar la primera imagen del plato
            holder.productImage.setImageResource(getImageResource(plato.getImagenes().get(0))); // Cargar la primera imagen
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

    // Método para obtener el plato por su ID (debes implementar esta lógica)
    private Plato obtenerPlatoPorId(String platoId) {
        // Aquí debes implementar la lógica para obtener el objeto Plato basado en su ID.
        // Esto puede ser a través de una base de datos, una lista en memoria, etc.
        return new Plato(platoId, "Hamburguesa", "DescripciónEjemplo", 10.0,
                List.of("burger_image"), "CategoriaEjemplo", false); // Cambia esto según tus necesidades
    }

    // Método para obtener el recurso de imagen (debes implementar esta lógica)
    private int getImageResource(String imagen) {
        // Aquí puedes usar el contexto para obtener el recurso de imagen.
        // Por ejemplo, usando `context.getResources().getIdentifier` para cargar imágenes por nombre
        int resourceId = context.getResources().getIdentifier(imagen, "drawable", context.getPackageName());
        return resourceId != 0 ? resourceId : R.drawable.burger_image; // Retorna una imagen por defecto si no se encuentra
    }
}
