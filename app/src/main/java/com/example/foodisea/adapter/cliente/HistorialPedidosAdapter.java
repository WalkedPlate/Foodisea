package com.example.foodisea.adapter.cliente;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodisea.R;
import com.example.foodisea.dto.PedidoConDetalles;
import com.example.foodisea.model.Pedido;
import com.example.foodisea.model.Restaurante;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistorialPedidosAdapter extends RecyclerView.Adapter<HistorialPedidosAdapter.PedidoViewHolder> {
    private List<PedidoConDetalles> pedidos;
    private final Context context;
    private final OnPedidoClickListener listener;

    public interface OnPedidoClickListener {
        void onPedidoClick(String pedidoId);
    }

    public HistorialPedidosAdapter(Context context, OnPedidoClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.pedidos = new ArrayList<>();
    }

    @NonNull
    @Override
    public PedidoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pedido_historial, parent, false);
        return new PedidoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PedidoViewHolder holder, int position) {
        PedidoConDetalles pedidoConDetalles = pedidos.get(position);
        Pedido pedido = pedidoConDetalles.getPedido();
        Restaurante restaurante = pedidoConDetalles.getRestaurante();

        // Establecer ID del pedido
        String pedidoId = pedido.getId();
        String displayId = pedidoId.length() >= 5
                ? pedidoId.substring(0, 5).toUpperCase()
                : pedidoId.toUpperCase();
        holder.tvPedidoId.setText(String.format("Pedido #%s", displayId));

        // Manejar fecha null de manera segura
        if (pedido.getFechaPedido() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            holder.tvFechaPedido.setText(dateFormat.format(pedido.getFechaPedido()));
        } else {
            holder.tvFechaPedido.setText("Fecha no disponible");
        }

        // Establecer nombre del restaurante
        holder.tvNombreRestaurante.setText(restaurante.getNombre());

        // Establecer estado del pedido
        holder.tvEstado.setText(pedido.getEstado());

        // Cargar imagen del restaurante
        if (restaurante.getImagenes() != null && !restaurante.getImagenes().isEmpty()) {
            Glide.with(context)
                    .load(restaurante.getImagenes().get(0))
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(holder.ivRestaurante);
        } else {
            holder.ivRestaurante.setImageResource(R.drawable.placeholder_image);
        }

        // Configurar click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPedidoClick(pedido.getId());
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPedidoClick(pedido.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return pedidos.size();
    }

    public void setPedidos(List<PedidoConDetalles> pedidos) {
        this.pedidos = pedidos;
        notifyDataSetChanged();
    }

    static class PedidoViewHolder extends RecyclerView.ViewHolder {
        TextView tvPedidoId;
        TextView tvFechaPedido;
        TextView tvNombreRestaurante;
        TextView tvEstado;
        ShapeableImageView ivRestaurante;

        PedidoViewHolder(View itemView) {
            super(itemView);
            tvPedidoId = itemView.findViewById(R.id.tvPedidoId);
            tvFechaPedido = itemView.findViewById(R.id.tvFechaPedido);
            tvNombreRestaurante = itemView.findViewById(R.id.tvNombreRestaurante);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            ivRestaurante = itemView.findViewById(R.id.ivRestaurante);
        }
    }
}
