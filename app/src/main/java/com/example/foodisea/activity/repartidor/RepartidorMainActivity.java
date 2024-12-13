package com.example.foodisea.activity.repartidor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.foodisea.R;
import com.example.foodisea.activity.login.LoginActivity;
import com.example.foodisea.adapter.repartidor.PedidosAdapter;
import com.example.foodisea.data.SessionManager;
import com.example.foodisea.databinding.ActivityRepartidorMainBinding;
import com.example.foodisea.dialog.LoadingDialog;
import com.example.foodisea.dto.PedidoConCliente;
import com.example.foodisea.model.Cliente;
import com.example.foodisea.model.Pedido;
import com.example.foodisea.model.Repartidor;
import com.example.foodisea.model.Usuario;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class RepartidorMainActivity extends AppCompatActivity {

    ActivityRepartidorMainBinding binding;
    private PedidosAdapter pedidosAdapter;
    private FirebaseFirestore db;
    private SessionManager sessionManager;
    private Repartidor repartidorActual;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();

        initializeComponents();
        setupRecyclerView();
        validateSession();
    }

    /**
     * Verifica la existencia de una sesión válida
     */
    private void validateSession() {
        //loadingDialog.show("Verificando sesión...");

        sessionManager.checkExistingSession(this, new SessionManager.SessionCallback() {
            @Override
            public void onSessionValid(Usuario usuario) {
                repartidorActual = sessionManager.getRepartidorActual();
                //loadingDialog.dismiss();

                initializeUI();
            }

            @Override
            public void onSessionError() {
                handleSessionError();
            }
        });
    }

    private void initializeComponents() {
        binding = ActivityRepartidorMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdge.enable(this);
        setupWindowInsets();

        sessionManager = SessionManager.getInstance(this);
        loadingDialog = new LoadingDialog(this);
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Inicializa la UI una vez que la sesión está validada
     */
    private void initializeUI() {
        setupUI();
        setupClickListeners();
    }

    /**
     * Configura la UI con los datos del usuario
     */
    private void setupUI() {
        try {
            String welcomeMessage = String.format("¡Hola %s!, ¿listo para repartir?",
                    repartidorActual.getNombres().split(" ")[0]);
            binding.tvWelcome.setText(welcomeMessage);
        } catch (Exception e) {
            // En caso de algún error con el nombre, usar mensaje genérico
            binding.tvWelcome.setText("¡Hola!, ¿listo para repartir?");
            Log.e("RepartidorMainActivity", "Error al configurar mensaje de bienvenida", e);
        }
    }

    private void setupClickListeners() {
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

    /**
     * Maneja errores de sesión
     */
    private void handleSessionError() {
        if (!isFinishing()) {
            loadingDialog.dismiss();
            sessionManager.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

}