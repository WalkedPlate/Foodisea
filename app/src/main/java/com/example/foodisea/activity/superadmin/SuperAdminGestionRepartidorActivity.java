package com.example.foodisea.activity.superadmin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodisea.R;
import com.example.foodisea.adapter.superAdmin.UsuarioAdapter;
import com.example.foodisea.databinding.ActivitySuperAdminGestionRepartidorBinding;
import com.example.foodisea.databinding.ActivitySuperAdminGestionRestauranteBinding;
import com.example.foodisea.databinding.ActivitySuperAdminGestionUsuariosBinding;
import com.example.foodisea.databinding.ActivitySuperAdminSolicitudesRepartidorBinding;
import com.example.foodisea.manager.LogManager;
import com.example.foodisea.model.Usuario;
import com.example.foodisea.repository.UsuarioRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SuperAdminGestionRepartidorActivity extends AppCompatActivity {
    ActivitySuperAdminGestionRepartidorBinding binding;
    UsuarioRepository usuarioRepository;
    private List<Usuario> listaUsuarios = new ArrayList<>();
    LogManager logManager = new LogManager();
    UsuarioAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeComponents();
        setupListeners();

        // Crear instancia de UsuarioRepository
        usuarioRepository = new UsuarioRepository();

        //Obtiene los usuarios de BD y los carga al Recicler View
        obtenerUsuarios();

        // Configurar SearchView
        binding.svRepartidores.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (adapter != null) {
                    adapter.filter(query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) {
                    adapter.filter(newText);
                }
                return true;
            }
        });
    }

    /**
     * Inicializa los componentes principales de la actividad
     */
    private void initializeComponents() {
        binding = ActivitySuperAdminGestionRepartidorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdge.enable(this);
        setupWindowInsets();
    }

    /**
     * Configura los insets de la ventana
     */
    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Configura los listeners de los botones
     */
    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnHome.setOnClickListener(v-> {
            Intent home = new Intent(this, SuperadminMainActivity.class);
            home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(home);
            finish();
        });
        binding.btnSolicitudes.setOnClickListener(v -> {
            Intent solicitudes = new Intent(this, SuperAdminSolicitudesRepartidorActivity.class);
            startActivity(solicitudes);
        });
    }


    public void obtenerUsuarios(){
        List<String> estadosPermitidos = Arrays.asList("Activo", "Inactivo");

        usuarioRepository.getUsuariosPorTipoYEstados("Repartidor", estadosPermitidos)
                .addOnSuccessListener(usuarios -> {
                    listaUsuarios = usuarios;
                    setupReciclerView();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar los usuarios: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("error", e.getMessage());
                });

    }

    public void setupReciclerView(){
        adapter = new UsuarioAdapter(this, listaUsuarios,usuarioRepository,logManager);
        binding.rvUsers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvUsers.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        obtenerUsuarios();  // Recarga los usuarios al volver a la actividad
    }
}