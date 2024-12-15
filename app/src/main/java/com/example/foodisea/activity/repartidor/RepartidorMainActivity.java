package com.example.foodisea.activity.repartidor;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodisea.R;
import com.example.foodisea.activity.login.LoginActivity;
import com.example.foodisea.adapter.repartidor.PedidosAdapter;
import com.example.foodisea.adapter.repartidor.RepartidorPedidosAdapter;
import com.example.foodisea.dto.PedidoConDetalles;
import com.example.foodisea.dto.PedidoConDistancia;
import com.example.foodisea.manager.SessionManager;
import com.example.foodisea.databinding.ActivityRepartidorMainBinding;
import com.example.foodisea.dialog.LoadingDialog;
import com.example.foodisea.dto.PedidoConCliente;
import com.example.foodisea.model.Cliente;
import com.example.foodisea.model.Pedido;
import com.example.foodisea.model.Repartidor;
import com.example.foodisea.model.Usuario;
import com.example.foodisea.repository.PedidoRepository;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class RepartidorMainActivity extends AppCompatActivity implements RepartidorPedidosAdapter.OnPedidoClickListener {

    private ActivityRepartidorMainBinding binding;
    private RepartidorPedidosAdapter pedidosAdapter;
    private PedidoRepository pedidoRepository;
    private SessionManager sessionManager;
    private Repartidor repartidorActual;
    private LoadingDialog loadingDialog;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeComponents();
        setupRecyclerView();
        validateSession();
    }

    private void initializeComponents() {
        binding = ActivityRepartidorMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdge.enable(this);
        setupWindowInsets();

        sessionManager = SessionManager.getInstance(this);
        loadingDialog = new LoadingDialog(this);
        pedidoRepository = new PedidoRepository(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void validateSession() {
        loadingDialog.show("Verificando sesión...");
        sessionManager.checkExistingSession(this, new SessionManager.SessionCallback() {
            @Override
            public void onSessionValid(Usuario usuario) {
                repartidorActual = sessionManager.getRepartidorActual();
                loadingDialog.dismiss();
                initializeUI();
                verificarPermisos();
            }

            @Override
            public void onSessionError() {
                handleSessionError();
            }
        });
    }

    private void initializeUI() {
        setupUI();
        setupClickListeners();
    }

    private void setupUI() {
        try {
            String welcomeMessage = String.format("¡Hola %s!, ¿listo para repartir?",
                    repartidorActual.getNombres().split(" ")[0]);
            binding.tvWelcome.setText(welcomeMessage);
        } catch (Exception e) {
            binding.tvWelcome.setText("¡Hola!, ¿listo para repartir?");
            Log.e("RepartidorMainActivity", "Error al configurar mensaje de bienvenida", e);
        }
    }

    private void setupClickListeners() {
        binding.btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, RepartidorPerfilActivity.class);
            startActivity(intent);
        });

        binding.btnOrders.setOnClickListener(v -> {
            Intent intent = new Intent(this, RepartidorPedidosActivosActivity.class);
            startActivity(intent);
        });

        // Pull to refresh
        binding.swipeRefreshLayout.setOnRefreshListener(this::refrescarPedidos);
    }


    private void setupRecyclerView() {
        binding.rvPedidos.setLayoutManager(new LinearLayoutManager(this));
        pedidosAdapter = new RepartidorPedidosAdapter(this, this);
        binding.rvPedidos.setAdapter(pedidosAdapter);
    }

    private void verificarPermisos() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            obtenerUbicacionYCargarPedidos();
        }
    }

    private void obtenerUbicacionYCargarPedidos() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            loadingDialog.show("Buscando pedidos cercanos...");
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            cargarPedidosCercanos(location.getLatitude(), location.getLongitude());
                        } else {
                            loadingDialog.dismiss();
                            Toast.makeText(this, "No se pudo obtener tu ubicación",
                                    Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        loadingDialog.dismiss();
                        Toast.makeText(this, "Error al obtener ubicación",
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void refrescarPedidos() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            cargarPedidosCercanos(location.getLatitude(), location.getLongitude());
                        } else {
                            binding.swipeRefreshLayout.setRefreshing(false);
                            Toast.makeText(this, "No se pudo obtener tu ubicación",
                                    Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        binding.swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(this, "Error al obtener ubicación",
                                Toast.LENGTH_SHORT).show();
                    });
        } else {
            binding.swipeRefreshLayout.setRefreshing(false);
            verificarPermisos();
        }
    }


    private void cargarPedidosCercanos(double latitud, double longitud) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvNoPedidos.setVisibility(View.GONE);

        pedidoRepository.getPedidosCercanosParaRepartidor(latitud, longitud)
                .addOnSuccessListener(pedidos -> {
                    if (isFinishing()) return;

                    loadingDialog.dismiss();
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefreshLayout.setRefreshing(false);

                    if (pedidos.isEmpty()) {
                        pedidosAdapter.setPedidos(new ArrayList<>());
                        binding.tvNoPedidos.setVisibility(View.VISIBLE);
                        binding.tvPedidosTitle.setText("Pedidos Disponibles (0)");
                    } else {
                        binding.tvNoPedidos.setVisibility(View.GONE);
                        binding.tvPedidosTitle.setText(
                                String.format("Pedidos Disponibles (%d)", pedidos.size())
                        );

                        // Aquí llamamos directamente a obtenerDetallesPedidos
                        obtenerDetallesPedidos(pedidos);
                    }
                })
                .addOnFailureListener(e -> {
                    if (isFinishing()) return;

                    loadingDialog.dismiss();
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefreshLayout.setRefreshing(false);
                    pedidosAdapter.setPedidos(new ArrayList<>());
                    Toast.makeText(this, "Error al cargar pedidos", Toast.LENGTH_SHORT).show();
                });
    }

    private void obtenerDetallesPedidos(List<Pedido> pedidosConDistancia) {
        List<Task<PedidoConDetalles>> tareas = pedidosConDistancia.stream()
                .map(pedido -> pedidoRepository.getPedidoConDetalles(pedido.getId()))
                .collect(Collectors.toList());

        Tasks.whenAllComplete(tareas)
                .addOnSuccessListener(tasks -> {
                    List<PedidoConDetalles> pedidosConDetalles = tareas.stream()
                            .map(task -> {
                                try {
                                    PedidoConDetalles detalles = ((Task<PedidoConDetalles>) task).getResult();
                                    return detalles;
                                } catch (Exception e) {
                                    return null;
                                }
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

                    pedidosAdapter.setPedidos(pedidosConDetalles);
                });
    }

    @Override
    public void onPedidoClick(PedidoConDetalles pedido) {
        Intent intent = new Intent(this, RepartidorVerPedidoActivity.class);
        intent.putExtra("pedidoId", pedido.getPedido().getId());
        startActivity(intent);
    }

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtenerUbicacionYCargarPedidos();
            } else {
                Toast.makeText(this, "Se requiere acceso a la ubicación",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static final int REQUEST_LOCATION_PERMISSION = 1;
}