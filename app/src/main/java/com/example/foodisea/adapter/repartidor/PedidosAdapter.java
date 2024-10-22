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

import com.example.foodisea.R;
import com.example.foodisea.activity.adminRes.AdminResPedidosActivity;
import com.example.foodisea.activity.repartidor.RepartidorRestauranteActivity;
import com.example.foodisea.activity.repartidor.RepartidorVerOrdenActivity;
import com.example.foodisea.model.Cliente;
import com.example.foodisea.model.Pago;
import com.example.foodisea.model.Pedido;

import java.util.List;
import java.util.Map;

public class PedidosAdapter extends RecyclerView.Adapter<PedidosAdapter.PedidoViewHolder> {

    private List<Pedido> pedidos;
    private Context context;
    private Map<String, Cliente> clientesMap;
    private Map<String, Pago> pagosMap;

    public PedidosAdapter(Context context, List<Pedido> pedidos, Map<String, Cliente> clientesMap, Map<String, Pago> pagosMap) {
        this.pedidos = pedidos;
        this.context = context;
        this.clientesMap = clientesMap;
        this.pagosMap = pagosMap;
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
        holder.bind(pedido, clientesMap.get(pedido.getClienteId()), pagosMap.get(pedido.getPagoId()));

        holder.itemView.setOnClickListener(v -> {
            if (context instanceof RepartidorRestauranteActivity) {
                Intent intent = new Intent(context, RepartidorVerOrdenActivity.class);
                intent.putExtra("pedidoId", pedido.getId());
                Cliente cliente = clientesMap.get(pedido.getClienteId());
                intent.putExtra("clienteNombre", cliente.getNombres() + " " + cliente.getApellidos());
                intent.putExtra("direccionEntrega", pedido.getDireccionEntrega());
                Pago pago = pagosMap.get(pedido.getPagoId());
                intent.putExtra("precio", pago.getMonto());
                context.startActivity(intent);
            } else if (context instanceof AdminResPedidosActivity) {
                ((AdminResPedidosActivity) context).mostrarBottonSheet(pedido);
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

        void bind(Pedido pedido, Cliente cliente, Pago pago) {
            tvOrderNumber.setText(pedido.getId());
            tvCustomerName.setText(cliente.getNombres() + " " + cliente.getApellidos());
            tvAddress.setText(pedido.getDireccionEntrega());
            tvPrice.setText(String.format("S/. %.2f", pago.getMonto()));


            // Actualizar el ícono basado en el estado del pedido
            /*
            switch (pedido.getEstado()) {
                case "Recibido":
                    ivDelivery.setImageResource(R.drawable.ic_order_received);
                    break;
                case "En preparación":
                    ivDelivery.setImageResource(R.drawable.ic_order_preparing);
                    break;
                case "En camino":
                    ivDelivery.setImageResource(R.drawable.ic_order_on_way);
                    break;
                case "Entregado":
                    ivDelivery.setImageResource(R.drawable.ic_order_delivered);
                    break;
                default:
                    ivDelivery.setImageResource(R.drawable.ic_order_default);
                    break;
            }

             */
        }
    }
}