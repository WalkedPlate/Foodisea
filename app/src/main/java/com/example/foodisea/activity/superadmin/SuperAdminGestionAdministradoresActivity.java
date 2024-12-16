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
import com.example.foodisea.databinding.ActivitySuperAdminGestionAdministradoresBinding;
import com.example.foodisea.databinding.ActivitySuperAdminGestionUsuariosBinding;
import com.example.foodisea.databinding.ActivitySuperAdminSelectionBinding;
import com.example.foodisea.manager.LogManager;
import com.example.foodisea.model.Usuario;
import com.example.foodisea.repository.UsuarioRepository;

import java.util.ArrayList;
import java.util.List;

public class SuperAdminGestionAdministradoresActivity extends AppCompatActivity {
    ActivitySuperAdminGestionAdministradoresBinding binding;
    UsuarioRepository usuarioRepository;
    private List<Usuario> listaUsuarios = new ArrayList<>();
    LogManager logManager = new LogManager();
    UsuarioAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySuperAdminGestionAdministradoresBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //funcion de botones:
        binding.btnBack.setOnClickListener(v -> {
            finish(); // Cierra la actividad actual y regresa
        });

        binding.btnHome.setOnClickListener(v-> {
            Intent home = new Intent(this, SuperadminMainActivity.class);
            home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(home);
            finish();
        });

        binding.btnAddAdminRes.setOnClickListener(v -> {
            Intent intent = new Intent(SuperAdminGestionAdministradoresActivity.this, SuperAdminAgregarAdministradorActivity.class);
            startActivity(intent);
        });

        // Crear instancia de UsuarioRepository
        usuarioRepository = new UsuarioRepository();

        //Obtiene los usuarios de BD y los carga al Recicler View
        obtenerUsuarios();

        // Configurar SearchView
        binding.svRestaurants.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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


    public void obtenerUsuarios(){
        usuarioRepository.getUsuariosPorTipo("AdministradorRestaurante")
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
}