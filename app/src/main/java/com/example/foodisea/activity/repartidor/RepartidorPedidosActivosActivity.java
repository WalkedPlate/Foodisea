package com.example.foodisea.activity.repartidor;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodisea.R;
import com.example.foodisea.activity.repartidor.fragment.HistorialPedidosFragment;
import com.example.foodisea.activity.repartidor.fragment.PedidoActivoFragment;
import com.example.foodisea.adapter.cliente.ViewPager2Adapter;
import com.example.foodisea.databinding.ActivityRepartidorPedidosActivosBinding;
import com.example.foodisea.dto.PedidoConDetalles;
import com.example.foodisea.manager.SessionManager;
import com.example.foodisea.model.Pedido;
import com.example.foodisea.model.Repartidor;
import com.example.foodisea.repository.PedidoRepository;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class RepartidorPedidosActivosActivity extends AppCompatActivity {
    private ActivityRepartidorPedidosActivosBinding binding;
    private PedidoRepository pedidoRepository;
    private SessionManager sessionManager;
    private Repartidor repartidorActual;

    private PedidoActivoFragment fragmentActivo;
    private HistorialPedidosFragment fragmentHistorial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeComponents();
        setupViewPager();
        setupListeners();
        cargarPedidos();
    }

    private void initializeComponents() {
        binding = ActivityRepartidorPedidosActivosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = SessionManager.getInstance(this);
        repartidorActual = sessionManager.getRepartidorActual();
        pedidoRepository = new PedidoRepository(this);

        fragmentActivo = new PedidoActivoFragment();
        fragmentHistorial = new HistorialPedidosFragment();

        EdgeToEdge.enable(this);
        setupWindowInsets();
    }

    private void setupViewPager() {
        ViewPager2Adapter pagerAdapter = new ViewPager2Adapter(this);
        pagerAdapter.addFragment(fragmentActivo, "Pedido Activo");
        pagerAdapter.addFragment(fragmentHistorial, "Historial");

        binding.viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> tab.setText(pagerAdapter.getPageTitle(position))
        ).attach();
    }

    private void cargarPedidos() {
        // Cargar pedido activo
        pedidoRepository.getPedidosActivosRepartidor(repartidorActual.getId())
                .addOnSuccessListener(pedidoActivo -> {
                    if (!pedidoActivo.isEmpty()) {
                        // Solo debería haber uno activo
                        pedidoRepository.getPedidoConDetalles(pedidoActivo.get(0).getId())
                                .addOnSuccessListener(detalles ->
                                        fragmentActivo.setPedidoActivo(detalles));
                    }
                });

        // Cargar historial de pedidos
        pedidoRepository.getPedidosEntregadosRepartidor(repartidorActual.getId())
                .addOnSuccessListener(pedidos -> {
                    List<Task<PedidoConDetalles>> detallesTasks = new ArrayList<>();
                    for (Pedido pedido : pedidos) {
                        detallesTasks.add(pedidoRepository.getPedidoConDetalles(pedido.getId()));
                    }

                    Tasks.whenAllComplete(detallesTasks)
                            .addOnSuccessListener(tasks -> {
                                List<PedidoConDetalles> pedidosConDetalles = new ArrayList<>();
                                for (Task<PedidoConDetalles> task : detallesTasks) {
                                    if (task.isSuccessful()) {
                                        pedidosConDetalles.add(task.getResult());
                                    }
                                }
                                fragmentHistorial.setPedidos(pedidosConDetalles);
                            });
                });
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