package com.example.foodisea.activity.repartidor.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.foodisea.R;
import com.example.foodisea.activity.repartidor.RepartidorDeliveryMapActivity;
import com.example.foodisea.databinding.FragmentPedidoActivoBinding;
import com.example.foodisea.dto.PedidoConDetalles;
import com.example.foodisea.model.Pedido;
import com.example.foodisea.model.Restaurante;
import com.example.foodisea.repository.ChatRepository;
import com.example.foodisea.repository.PedidoRepository;

public class PedidoActivoFragment extends Fragment {
    private FragmentPedidoActivoBinding binding;
    private PedidoConDetalles pedidoActual;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPedidoActivoBinding.inflate(inflater, container, false);
        setupListeners();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupListeners() {
        binding.btnAccion.setOnClickListener(v -> manejarAccionPedido());

        // Agregar click listener para toda la tarjeta del pedido
        binding.cardPedidoActivo.setOnClickListener(v -> {
            if (pedidoActual != null) {
                ChatRepository chatRepository = new ChatRepository();
                chatRepository.getChatByPedidoId(pedidoActual.getPedido().getId(),
                        chat -> {
                            Intent intent = new Intent(requireContext(), RepartidorDeliveryMapActivity.class);
                            intent.putExtra("pedidoId", pedidoActual.getPedido().getId());
                            intent.putExtra("chatId", chat.getId());
                            startActivity(intent);
                        },
                        error -> {
                            Toast.makeText(requireContext(),
                                    "Error al obtener el chat del pedido",
                                    Toast.LENGTH_SHORT).show();
                        }
                );
            }
        });
    }

    public void setPedidoActivo(PedidoConDetalles pedido) {
        this.pedidoActual = pedido;
        if (pedido == null) {
            mostrarEstadoSinPedido();
            return;
        }
        mostrarPedidoActivo();
    }

    private void mostrarEstadoSinPedido() {
        binding.layoutNoPedido.setVisibility(View.VISIBLE);
        binding.cardPedidoActivo.setVisibility(View.GONE);
    }

    private void mostrarPedidoActivo() {
        binding.layoutNoPedido.setVisibility(View.GONE);
        binding.cardPedidoActivo.setVisibility(View.VISIBLE);

        // Mostrar datos del restaurante
        Restaurante restaurante = pedidoActual.getRestaurante();
        binding.tvNombreRestaurante.setText(restaurante.getNombre());
        binding.tvDireccionRestaurante.setText(restaurante.getDireccion());

        // Cargar imagen del restaurante
        if (restaurante.getImagenes() != null && !restaurante.getImagenes().isEmpty()) {
            Glide.with(requireContext())
                    .load(restaurante.getImagenes().get(0))
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(binding.ivRestaurante);
        }

        // Mostrar estado y dirección de entrega
        Pedido pedido = pedidoActual.getPedido();
        binding.tvEstadoPedido.setText(pedido.getEstado());
        binding.tvDireccionEntrega.setText(pedido.getDireccionEntrega());

        // Configurar botón según estado
        configurarBotonEstado(pedido.getEstado());
    }

    private void configurarBotonEstado(String estado) {
        switch (estado) {
            case "Recogiendo pedido":
                binding.btnAccion.setText("Recoger pedido");
                binding.btnAccion.setEnabled(true);
                break;
            case "En camino":
                binding.btnAccion.setText("Entregar pedido");
                binding.btnAccion.setEnabled(true);
                break;
            default:
                binding.btnAccion.setEnabled(false);
                break;
        }
    }

    private void manejarAccionPedido() {
        if (pedidoActual == null) return;

        String estadoActual = pedidoActual.getPedido().getEstado();
        String nuevoEstado;

        switch (estadoActual) {
            case "Recogiendo pedido":
                nuevoEstado = "En camino";
                break;
            case "En camino":
                nuevoEstado = "Entregado";
                break;
            default:
                return;
        }

        // Actualizar estado en Firebase
        new PedidoRepository(requireContext())
                .actualizarEstadoPedido(pedidoActual.getPedido().getId(), nuevoEstado)
                .addOnSuccessListener(aVoid -> {
                    // La actualización del UI se hará a través del listener en el Activity
                });
    }

    private void irATrackingPedido() {
        if (pedidoActual == null) return;

        ChatRepository chatRepository = new ChatRepository();
        chatRepository.getChatByPedidoId(pedidoActual.getPedido().getId(),
                chat -> {
                    Intent intent = new Intent(requireContext(), RepartidorDeliveryMapActivity.class);
                    intent.putExtra("pedidoId", pedidoActual.getPedido().getId());
                    intent.putExtra("chatId", chat.getId());
                    startActivity(intent);
                },
                error -> {
                    Toast.makeText(requireContext(),
                            "Error al obtener el chat del pedido",
                            Toast.LENGTH_SHORT).show();
                }
        );
    }

}