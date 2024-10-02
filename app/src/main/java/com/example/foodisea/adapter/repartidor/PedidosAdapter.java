package com.example.foodisea.adapter.repartidor;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodisea.activity.adminRes.AdminResPedidosActivity;
import com.example.foodisea.R;
import com.example.foodisea.activity.repartidor.RepartidorRestauranteActivity;
import com.example.foodisea.activity.repartidor.RepartidorVerOrdenActivity;
import com.example.foodisea.model.Pedido;

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
            
            if(context instanceof RepartidorRestauranteActivity) {
                Intent intent = new Intent(context, RepartidorVerOrdenActivity.class);
                intent.putExtra("pedidoId", pedido.getId());
                intent.putExtra("clienteNombre", pedido.getCliente().getNombres() + " " + pedido.getCliente().getApellidos());
                intent.putExtra("direccionEntrega", pedido.getDireccionEntrega());
                intent.putExtra("precio", pedido.getPago().getMonto());
                context.startActivity(intent);    
            } else if (context instanceof AdminResPedidosActivity) {
                ((AdminResPedidosActivity)context).mostrarBottonSheet(pedido);
            }

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
            tvOrderNumber.setText(pedido.getId());  // Usando el ID del pedido como el número del pedido
            tvCustomerName.setText(pedido.getCliente().getNombres() + " " + pedido.getCliente().getApellidos());
            tvAddress.setText(pedido.getDireccionEntrega());
            tvPrice.setText(String.format("S/. %.2f", pedido.getPago().getMonto()));
            // Si el ícono de entrega cambia basado en el estado del pedido
            // ivDelivery.setImageResource(R.drawable.ic_delivery);
        }
    }
}
