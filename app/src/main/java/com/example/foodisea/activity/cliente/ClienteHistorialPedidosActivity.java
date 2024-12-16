package com.example.foodisea.activity.cliente;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodisea.R;
import com.example.foodisea.activity.cliente.fragment.PedidosEnCursoFragment;
import com.example.foodisea.adapter.cliente.ViewPager2Adapter;
import com.example.foodisea.databinding.ActivityClienteHistorialPedidosBinding;
import com.example.foodisea.dto.PedidoConDetalles;
import com.example.foodisea.manager.SessionManager;
import com.example.foodisea.model.Cliente;
import com.example.foodisea.model.Pedido;
import com.example.foodisea.repository.PedidoRepository;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class ClienteHistorialPedidosActivity extends AppCompatActivity {
    private static final String TAG = "HistorialPedidos";
    private ActivityClienteHistorialPedidosBinding binding;
    private PedidoRepository pedidoRepository;
    private SessionManager sessionManager;
    private Cliente clienteActual;

    // Fragments para cada sección
    private PedidosEnCursoFragment fragmentEnCurso;
    private PedidosEnCursoFragment fragmentEntregados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeComponents();
        setupViewPager();
        setupListeners();
        cargarPedidos();
    }

    private void initializeComponents() {
        binding = ActivityClienteHistorialPedidosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = SessionManager.getInstance(this);
        clienteActual = sessionManager.getClienteActual();
        pedidoRepository = new PedidoRepository(this);

        // Inicializar fragments
        fragmentEnCurso = new PedidosEnCursoFragment();
        fragmentEntregados = new PedidosEnCursoFragment();

        EdgeToEdge.enable(this);
        setupWindowInsets();
    }

    private void setupViewPager() {
        ViewPager2Adapter pagerAdapter = new ViewPager2Adapter(this);
        pagerAdapter.addFragment(fragmentEnCurso, "En Curso");
        pagerAdapter.addFragment(fragmentEntregados, "Entregados");

        binding.viewPager.setAdapter(pagerAdapter);

        // Conectar TabLayout con ViewPager2
        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> tab.setText(pagerAdapter.getPageTitle(position))
        ).attach();
    }

    private void cargarPedidos() {
        Log.d(TAG, "Iniciando carga de pedidos para cliente: " + clienteActual.getId());

        pedidoRepository.getPedidosPorCliente(clienteActual.getId())
                .addOnSuccessListener(pedidos -> {
                    Log.d(TAG, "Pedidos recuperados: " + pedidos.size());
                    List<Task<PedidoConDetalles>> detallesTasks = new ArrayList<>();

                    for (Pedido pedido : pedidos) {
                        Log.d(TAG, "Procesando pedido ID: " + pedido.getId() + " Estado: " + pedido.getEstado());
                        detallesTasks.add(pedidoRepository.getPedidoConDetalles(pedido.getId()));
                    }

                    Tasks.whenAllComplete(detallesTasks)
                            .addOnSuccessListener(tasks -> {
                                List<PedidoConDetalles> pedidosEnCurso = new ArrayList<>();
                                List<PedidoConDetalles> pedidosEntregados = new ArrayList<>();

                                for (Task<PedidoConDetalles> task : detallesTasks) {
                                    if (task.isSuccessful()) {
                                        PedidoConDetalles pedidoConDetalles = task.getResult();
                                        String estado = pedidoConDetalles.getPedido().getEstado();
                                        Log.d(TAG, "Clasificando pedido: " + pedidoConDetalles.getPedido().getId() +
                                                " Estado: " + estado);

                                        if ("Entregado".equals(estado)) {
                                            pedidosEntregados.add(pedidoConDetalles);
                                        } else {
                                            pedidosEnCurso.add(pedidoConDetalles);
                                        }
                                    } else {
                                        Log.e(TAG, "Error al obtener detalles del pedido", task.getException());
                                    }
                                }

                                Log.d(TAG, "Pedidos clasificados - En curso: " + pedidosEnCurso.size() +
                                        " Entregados: " + pedidosEntregados.size());

                                // Actualizar los fragments
                                fragmentEnCurso.setPedidos(pedidosEnCurso);
                                fragmentEntregados.setPedidos(pedidosEntregados);
                            })
                            .addOnFailureListener(e ->
                                    Log.e(TAG, "Error al procesar todos los pedidos", e)
                            );
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al obtener pedidos del cliente", e)
                );
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
    }
}