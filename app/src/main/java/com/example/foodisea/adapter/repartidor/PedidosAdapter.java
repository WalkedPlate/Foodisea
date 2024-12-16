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
import com.example.foodisea.activity.repartidor.RepartidorVerPedidoActivity;
import com.example.foodisea.dto.PedidoConCliente;

import java.util.List;

public class PedidosAdapter extends RecyclerView.Adapter<PedidosAdapter.PedidoViewHolder> {

    private List<PedidoConCliente> pedidosConCliente;
    private Context context;
//    private Map<String, Cliente> clientesMap;
//    private Map<String, Pago> pagosMap;


    public PedidosAdapter(List<PedidoConCliente> pedidosConCliente, Context context) {
        this.pedidosConCliente = pedidosConCliente;
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
        PedidoConCliente pedidoConCliente = pedidosConCliente.get(position);

        holder.bind(pedidoConCliente);

        holder.itemView.setOnClickListener(v -> {
            if (context instanceof RepartidorRestauranteActivity) {
                Intent intent = new Intent(context, RepartidorVerPedidoActivity.class);
                intent.putExtra("pedidoId", pedidoConCliente.getPedido().getId());
                //Cliente cliente = clientesMap.get(pedido.getClienteId());
                intent.putExtra("clienteNombre", pedidoConCliente.getCliente().obtenerNombreCompleto());
                intent.putExtra("direccionEntrega", pedidoConCliente.getPedido().getDireccionEntrega());
                //Pago pago = pagosMap.get(pedido.getPagoId());
                intent.putExtra("precio", pedidoConCliente.getPedido().getMontoTotal());
                context.startActivity(intent);
            } else if (context instanceof AdminResPedidosActivity) {
                //((AdminResPedidosActivity) context).mostrarBottonSheet(pedidoConCliente.getPedido());
                ((AdminResPedidosActivity) context).mostrarBottonSheet(pedidoConCliente);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pedidosConCliente.size();
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

        void bind(PedidoConCliente pedidoConCliente) {
            String pedidoId = pedidoConCliente.getPedido().getId();
            String displayId = pedidoId.length() >= 5
                    ? pedidoId.substring(0, 5).toUpperCase()
                    : pedidoId.toUpperCase();
            tvOrderNumber.setText(String.format("#%s", displayId));

            tvCustomerName.setText(pedidoConCliente.getCliente().obtenerNombreCompleto());
            tvAddress.setText(pedidoConCliente.getPedido().getDireccionEntrega());
            tvPrice.setText(String.format("S/. %.2f", pedidoConCliente.getPedido().getMontoTotal()));


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