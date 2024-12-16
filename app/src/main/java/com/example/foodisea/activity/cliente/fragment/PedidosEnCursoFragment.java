package com.example.foodisea.activity.cliente.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.foodisea.R;
import com.example.foodisea.activity.cliente.ClienteTrackingActivity;
import com.example.foodisea.adapter.cliente.HistorialPedidosAdapter;
import com.example.foodisea.dto.PedidoConDetalles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


public class PedidosEnCursoFragment extends Fragment {
    private RecyclerView rvPedidos;
    private HistorialPedidosAdapter adapter;
    private List<PedidoConDetalles> allPedidos = new ArrayList<>();
    private static final int ITEMS_PER_PAGE = 10;
    private int currentItems = ITEMS_PER_PAGE;
    private Button btnVerMas;
    private boolean datosListos = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pedidos_en_curso, container, false);

        initializeComponents(view);
        setupRecyclerView();

        // Si los datos llegaron antes que la vista, mostrarlos ahora
        if (datosListos) {
            mostrarPedidosActuales();
        }

        return view;
    }

    private void initializeComponents(View view) {
        rvPedidos = view.findViewById(R.id.rvPedidos);
        btnVerMas = view.findViewById(R.id.btnVerMas);

        if (btnVerMas != null) {
            btnVerMas.setOnClickListener(v -> cargarMasPedidos());
        }
    }

    private void setupRecyclerView() {
        adapter = new HistorialPedidosAdapter(requireContext(), pedidoId -> {
            Intent intent = new Intent(requireContext(), ClienteTrackingActivity.class);
            intent.putExtra("pedidoId", pedidoId);
            startActivity(intent);
        });

        rvPedidos.setAdapter(adapter);
        rvPedidos.setLayoutManager(new LinearLayoutManager(requireContext()));
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

            // Actualizar visibilidad del botón solo si existe
            if (btnVerMas != null) {
                btnVerMas.setVisibility(allPedidos.size() > currentItems ? View.VISIBLE : View.GONE);
            }
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
            return fecha2.compareTo(fecha1); // Orden descendente (más reciente primero)
        });
        return ordenados;
    }
}