package com.example.foodisea.adapter.repartidor;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodisea.R;
import com.example.foodisea.dto.PedidoConCliente;
import com.example.foodisea.dto.PedidoConDetalles;
import com.example.foodisea.model.Cliente;
import com.example.foodisea.model.Pedido;
import com.example.foodisea.model.Restaurante;
import com.example.foodisea.repository.RestauranteRepository;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RepartidorPedidosAdapter extends RecyclerView.Adapter<RepartidorPedidosAdapter.PedidoViewHolder> {

    private List<PedidoConDetalles> pedidos;
    private final Context context;
    private final OnPedidoClickListener listener;

    public interface OnPedidoClickListener {
        void onPedidoClick(PedidoConDetalles pedido);
    }

    public RepartidorPedidosAdapter(Context context, OnPedidoClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.pedidos = new ArrayList<>();
    }

    public void setPedidos(List<PedidoConDetalles> pedidos) {
        this.pedidos = pedidos;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PedidoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pedido_repartidor, parent, false);
        return new PedidoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PedidoViewHolder holder, int position) {
        holder.bind(pedidos.get(position));
    }

    @Override
    public int getItemCount() {
        return pedidos.size();
    }

    class PedidoViewHolder extends RecyclerView.ViewHolder {
        private final Chip chipEstado;
        private final TextView tvOrderId, tvDistancia;
        private final TextView tvRestaurantName, tvAddress, tvMontoTotal, tvFechaPedido;

        PedidoViewHolder(View itemView) {
            super(itemView);
            chipEstado = itemView.findViewById(R.id.chipEstado);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvDistancia = itemView.findViewById(R.id.tvDistancia);
            tvRestaurantName = itemView.findViewById(R.id.tvRestaurantName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvMontoTotal = itemView.findViewById(R.id.tvMontoTotal);
            tvFechaPedido = itemView.findViewById(R.id.tvFechaPedido);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onPedidoClick(pedidos.get(position));
                }
            });
        }

        void bind(PedidoConDetalles pedidoConDetalles) {
            Pedido pedido = pedidoConDetalles.getPedido();
            Restaurante restaurante = pedidoConDetalles.getRestaurante();

            // Configurar estado
            chipEstado.setText(pedido.getEstado());
            chipEstado.setChipBackgroundColorResource(R.color.bg_gray_light);
            chipEstado.setTextColor(ContextCompat.getColor(context, R.color.btn_medium));

            // Configurar ID y distancia
            tvOrderId.setText(String.format("#%s", pedido.getId()));

            // Mostrar la distancia calculada
            double distancia = pedidoConDetalles.getDistancia();
            tvDistancia.setText(String.format(Locale.getDefault(), "%.1f km", distancia));

            // Info del restaurante y dirección
            tvRestaurantName.setText(restaurante.getNombre());
            tvAddress.setText(pedido.getDireccionEntrega());

            // Monto y fecha
            tvMontoTotal.setText(String.format("S/. %.2f", pedido.getMontoTotal()));
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            tvFechaPedido.setText(sdf.format(pedido.getFechaPedido()));
        }


    }
}
