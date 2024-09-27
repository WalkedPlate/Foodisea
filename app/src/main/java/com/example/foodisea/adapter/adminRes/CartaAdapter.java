package com.example.foodisea.adapter.adminRes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodisea.R;
import com.example.foodisea.entity.Producto;

import java.util.List;

public class CartaAdapter extends RecyclerView.Adapter<CartaAdapter.ProductoViewHolder> {
    private List<Producto> listaProductos;
    private Context context;

    public List<Producto> getListaProductos() {
        return listaProductos;
    }

    public void setListaProductos(List<Producto> listaProductos) {
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
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_res_carta,parent,false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto p = listaProductos.get(position);
        holder.producto = p;

        TextView textViewNombreProducto = holder.itemView.findViewById(R.id.nameProducto);
        textViewNombreProducto.setText(p.getName());

        TextView textViewPrecio = holder.itemView.findViewById(R.id.precioProducto);
        textViewPrecio.setText("$" + p.getPrice());

        ImageView imageViewProducto = holder.itemView.findViewById(R.id.platoImage);
        imageViewProducto.setImageResource(p.getImageResource());

        // Mostrar "Agotado" si el producto no está disponible
        if (p.isOutOfStock()) {
            TextView textViewStatus = holder.itemView.findViewById(R.id.productStatus);
            textViewStatus.setVisibility(View.VISIBLE);
        } else {
            TextView textViewStatus = holder.itemView.findViewById(R.id.productStatus);
            textViewStatus.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount(){
        return listaProductos.size();
    }

    public class ProductoViewHolder extends RecyclerView.ViewHolder{
        Producto producto;

        public ProductoViewHolder(@NonNull View itemView){
            super(itemView);
        }
    }
}
