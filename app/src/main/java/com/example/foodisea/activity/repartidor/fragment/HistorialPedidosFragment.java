package com.example.foodisea.activity.repartidor.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.foodisea.R;
import com.example.foodisea.activity.repartidor.RepartidorDeliveryMapActivity;
import com.example.foodisea.adapter.cliente.HistorialPedidosAdapter;
import com.example.foodisea.databinding.FragmentHistorialPedidosBinding;
import com.example.foodisea.dto.PedidoConDetalles;
import com.example.foodisea.repository.ChatRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class HistorialPedidosFragment extends Fragment {
    private FragmentHistorialPedidosBinding binding;
    private HistorialPedidosAdapter adapter;
    private List<PedidoConDetalles> allPedidos = new ArrayList<>();
    private static final int ITEMS_PER_PAGE = 10;
    private int currentItems = ITEMS_PER_PAGE;
    private boolean datosListos = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHistorialPedidosBinding.inflate(inflater, container, false);
        setupRecyclerView();

        if (datosListos) {
            mostrarPedidosActuales();
        }

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupRecyclerView() {
        adapter = new HistorialPedidosAdapter(requireContext(), pedidoId -> {
            // Buscar el PedidoConDetalles correspondiente al pedidoId
            PedidoConDetalles pedidoSeleccionado = null;
            for (PedidoConDetalles pedido : allPedidos) {
                if (pedido.getPedido().getId().equals(pedidoId)) {
                    pedidoSeleccionado = pedido;
                    break;
                }
            }

            if (pedidoSeleccionado != null) {
                final PedidoConDetalles pedidoFinal = pedidoSeleccionado;
                ChatRepository chatRepository = new ChatRepository();
                chatRepository.getChatByPedidoId(pedidoId,
                        chat -> {
                            Intent intent = new Intent(requireContext(), RepartidorDeliveryMapActivity.class);
                            intent.putExtra("pedidoId", pedidoId);
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

        binding.rvPedidos.setAdapter(adapter);
        binding.rvPedidos.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.btnVerMas.setOnClickListener(v -> cargarMasPedidos());
    }

    public void setPedidos(List<PedidoConDetalles> pedidos) {
        this.allPedidos = ordenarPedidosPorFecha(pedidos);
        datosListos = true;

        if (isAdded() && getView() != null) {
            mostrarPedidosActuales();
        }
    }

    private void mostrarPedidosActuales() {
        if (adapter != null) {
            int itemsToShow = Math.min(currentItems, allPedidos.size());
            adapter.setPedidos(new ArrayList<>(allPedidos.subList(0, itemsToShow)));
            binding.btnVerMas.setVisibility(allPedidos.size() > currentItems ? View.VISIBLE : View.GONE);
        }
    }

    private void cargarMasPedidos() {
        currentItems += ITEMS_PER_PAGE;
        mostrarPedidosActuales();
    }

    private List<PedidoConDetalles> ordenarPedidosPorFecha(List<PedidoConDetalles> pedidos) {
        List<PedidoConDetalles> ordenados = new ArrayList<>(pedidos);
        Collections.sort(ordenados, (p1, p2) -> {
            Date fecha1 = p1.getPedido().getFechaPedido();
            Date fecha2 = p2.getPedido().getFechaPedido();
            if (fecha1 == null) return 1;
            if (fecha2 == null) return -1;
            return fecha2.compareTo(fecha1);
        });
        return ordenados;
    }

    private void navegarATracking(PedidoConDetalles pedidoConDetalles) {
        ChatRepository chatRepository = new ChatRepository();
        chatRepository.getChatByPedidoId(pedidoConDetalles.getPedido().getId(),
                chat -> {
                    // Al obtener el chat exitosamente, navegamos con ambos IDs
                    Intent intent = new Intent(requireContext(), RepartidorDeliveryMapActivity.class);
                    intent.putExtra("pedidoId", pedidoConDetalles.getPedido().getId());
                    intent.putExtra("chatId", chat.getId());
                    startActivity(intent);
                },
                error -> {
                    // Manejar el error si no se encuentra el chat
                    Toast.makeText(requireContext(),
                            "Error al obtener el chat del pedido",
                            Toast.LENGTH_SHORT).show();
                }
        );
    }
}