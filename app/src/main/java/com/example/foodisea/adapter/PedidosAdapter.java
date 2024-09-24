package com.example.foodisea.adapter;

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
import com.example.foodisea.activity.repartidor.RepartidorRestauranteActivity;
import com.example.foodisea.activity.repartidor.RepartidorVerOrdenActivity;
import com.example.foodisea.entity.Pedido;

import java.util.List;

public class PedidosAdapter extends RecyclerView.Adapter<PedidosAdapter.PedidoViewHolder> {

    private List<Pedido> pedidos;
    private Context context;

    public PedidosAdapter(Context context, List<Pedido> pedidos) {
        this.pedidos = pedidos;
        this.context = context;
    }

    @NonNull
    @Override
    public PedidoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pedido, parent, false);
        return new PedidoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PedidoViewHolder holder, int position) {
        Pedido pedido = pedidos.get(position);
        holder.bind(pedido);

        // Añadir el onClickListener para abrir la actividad de detalles
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, RepartidorVerOrdenActivity.class);
            intent.putExtra("orderNumber", pedido.getOrderNumber());
            intent.putExtra("customerName", pedido.getCustomerName());
            intent.putExtra("address", pedido.getAddress());
            intent.putExtra("price", pedido.getPrice());
            context.startActivity(intent);
        });


    }

    @Override
    public int getItemCount() {
        return pedidos.size();
    }

    static class PedidoViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderNumber, tvCustomerName, tvAddress, tvPrice;
        ImageView ivDelivery;

        PedidoViewHolder(View itemView) {
            super(itemView);
            tvOrderNumber = itemView.findViewById(R.id.tvOrderNumber);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            ivDelivery = itemView.findViewById(R.id.ivDelivery);
        }

        void bind(Pedido pedido) {
            tvOrderNumber.setText(pedido.getOrderNumber());
            tvCustomerName.setText(pedido.getCustomerName());
            tvAddress.setText(pedido.getAddress());
            tvPrice.setText(String.format("S/. %.2f", pedido.getPrice()));
            // You can set the delivery icon here if it changes based on the order
            // ivDelivery.setImageResource(R.drawable.ic_delivery);
        }
    }
}
