package com.example.foodisea.activity.repartidor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.foodisea.R;
import com.example.foodisea.adapter.repartidor.PedidosAdapter;
import com.example.foodisea.databinding.ActivityRepartidorMainBinding;
import com.example.foodisea.dto.PedidoConCliente;
import com.example.foodisea.model.Cliente;
import com.example.foodisea.model.Pedido;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class RepartidorMainActivity extends AppCompatActivity {

    ActivityRepartidorMainBinding binding;
    private PedidosAdapter pedidosAdapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();

        initializeComponents();
        setupButtonListeners();
        setupRecyclerView();
    }

    private void initializeComponents() {
        binding = ActivityRepartidorMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdge.enable(this);
        setupWindowInsets();
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupButtonListeners() {
        //Botón del perfil
        binding.btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, RepartidorPerfilActivity.class);
            startActivity(intent);
        });

        //Botón para dar seguimiento del delivery
        binding.btnOrders.setOnClickListener(v -> {

        });
    }

    private void setupRecyclerView() {
        binding.rvPedidos.setLayoutManager(new GridLayoutManager(this, 2));
        pedidosAdapter = new PedidosAdapter(new ArrayList<>(), this);
        binding.rvPedidos.setAdapter(pedidosAdapter);

        cargarPedidos();
    }

    private void cargarPedidos() {
        db.collection("pedidos").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<PedidoConCliente> pedidosConCliente = new ArrayList<>();

                        for (DocumentSnapshot document : task.getResult()) {
                            Pedido pedido = document.toObject(Pedido.class);
                            String clienteId = pedido.getClienteId();

                            db.collection("usuarios").document(clienteId).get()
                                    .addOnSuccessListener(clienteDoc -> {
                                        Cliente cliente = clienteDoc.toObject(Cliente.class);
                                        if (cliente != null) {
                                            pedidosConCliente.add(new PedidoConCliente(pedido, cliente));
                                            pedidosAdapter.notifyDataSetChanged();

                                            // Actualizar el título con la cantidad de pedidos
                                            binding.tvPedidosTitle.setText(
                                                    String.format("Pedidos (%d)", pedidosConCliente.size())
                                            );
                                        }
                                    });
                        }

                        pedidosAdapter = new PedidosAdapter(pedidosConCliente, this);
                        binding.rvPedidos.setAdapter(pedidosAdapter);

                    } else {
                        binding.tvNoPedidos.setVisibility(View.VISIBLE);
                    }
                });
    }


}