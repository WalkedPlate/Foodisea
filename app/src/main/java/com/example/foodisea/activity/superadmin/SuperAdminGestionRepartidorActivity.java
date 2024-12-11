package com.example.foodisea.activity.superadmin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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
import com.example.foodisea.model.Usuario;
import com.example.foodisea.repository.UsuarioRepository;

import java.util.ArrayList;
import java.util.List;

public class SuperAdminGestionRepartidorActivity extends AppCompatActivity {
    ActivitySuperAdminGestionRepartidorBinding binding;
    UsuarioRepository usuarioRepository;
    private List<Usuario> listaUsuarios = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeComponents();
        setupListeners();

        // Crear instancia de UsuarioRepository
        usuarioRepository = new UsuarioRepository();

        //Obtiene los usuarios de BD y los carga al Recicler View
        obtenerUsuarios();
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

    // Método que devuelve una lista de usuarios de ejemplo
    private List<Usuario> getUsuariosList() {
        List<Usuario> usuarioList = new ArrayList<>();

        // Agregar usuarios de diferentes tipos
        usuarioList.add(new Usuario("1", "Juan", "Pérez", "juan.perez@mail.com", "123456789", "123 Calle Falsa", "12345678", "01/01/1990", "", "Activo", "Cliente"));
        usuarioList.add(new Usuario("2", "María", "Gómez", "maria.gomez@mail.com", "987654321", "456 Avenida Real", "87654321", "02/02/1985", "", "Inactivo", "Cliente"));
        usuarioList.add(new Usuario("3", "Carlos", "López", "carlos.lopez@mail.com", "112233445", "789 Calle Principal", "65432189", "03/03/1988", "", "Activo", "Repartidor"));
        usuarioList.add(new Usuario("4", "Ana", "Ramírez", "ana.ramirez@mail.com", "123789456", "321 Calle Secundaria", "13254687", "04/04/1991", "", "Activo", "AdministradorRestaurante"));
        usuarioList.add(new Usuario("4", "Ana", "Ramírez", "ana.ramirez@mail.com", "123789456", "321 Calle Secundaria", "13254687", "04/04/1991", "", "Activo", "Repartidor"));
        usuarioList.add(new Usuario("4", "Ana", "Ramírez", "ana.ramirez@mail.com", "123789456", "321 Calle Secundaria", "13254687", "04/04/1991", "", "Activo", "AdministradorRestaurante"));
        usuarioList.add(new Usuario("4", "Ana", "Ramírez", "ana.ramirez@mail.com", "123789456", "321 Calle Secundaria", "13254687", "04/04/1991", "", "Activo", "Repartidor"));
        usuarioList.add(new Usuario("4", "Ana", "Ramírez", "ana.ramirez@mail.com", "123789456", "321 Calle Secundaria", "13254687", "04/04/1991", "", "Activo", "AdministradorRestaurante"));

        // Filtrar solo los usuarios de tipo "Cliente"
        List<Usuario> usuariosClientes = new ArrayList<>();
        for (Usuario usuario : usuarioList) {
            if ("Repartidor".equals(usuario.getTipoUsuario())) {
                usuariosClientes.add(usuario);
            }
        }

        return usuariosClientes;
    }

    public void obtenerUsuarios(){
        usuarioRepository.getUsuariosPorTipo("Repartidor")
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
        UsuarioAdapter adapter = new UsuarioAdapter(this, listaUsuarios,usuarioRepository);
        binding.rvUsers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvUsers.setAdapter(adapter);
    }
}