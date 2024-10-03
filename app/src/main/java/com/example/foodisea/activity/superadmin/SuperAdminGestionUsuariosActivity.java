package com.example.foodisea.activity.superadmin;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodisea.R;
import com.example.foodisea.adapter.cliente.RestauranteAdapter;
import com.example.foodisea.adapter.superAdmin.UsuarioAdapter;
import com.example.foodisea.databinding.ActivitySuperAdminGestionUsuariosBinding;
import com.example.foodisea.databinding.ActivitySuperadminMainBinding;
import com.example.foodisea.model.Restaurante;
import com.example.foodisea.model.Usuario;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SuperAdminGestionUsuariosActivity extends AppCompatActivity {

    ActivitySuperAdminGestionUsuariosBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySuperAdminGestionUsuariosBinding.inflate(getLayoutInflater());
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

        binding.btnHome.setOnClickListener(v -> {
            Intent home = new Intent(this, SuperadminMainActivity.class);
            startActivity(home);
        });

        // Configurar el adaptador
        UsuarioAdapter adapter = new UsuarioAdapter(this, getUsuariosList());
        binding.rvUsers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvUsers.setAdapter(adapter);
    }

    // Método que devuelve una lista de usuarios de ejemplo
    private List<Usuario> getUsuariosList() {
        List<Usuario> usuarioList = new ArrayList<>();

        // Agregar usuarios de diferentes tipos
        usuarioList.add(new Usuario("1", "Juan", "Pérez", "juan.perez@mail.com", "123456789", "123 Calle Falsa", "12345678", "01/01/1990", "", "Activo", "Cliente"));
        usuarioList.add(new Usuario("2", "María", "Gómez", "maria.gomez@mail.com", "987654321", "456 Avenida Real", "87654321", "02/02/1985", "", "Inactivo", "Cliente"));
        usuarioList.add(new Usuario("3", "Carlos", "López", "carlos.lopez@mail.com", "112233445", "789 Calle Principal", "65432189", "03/03/1988", "", "Activo", "Repartidor"));
        usuarioList.add(new Usuario("4", "Ana", "Ramírez", "ana.ramirez@mail.com", "123789456", "321 Calle Secundaria", "13254687", "04/04/1991", "", "Activo", "AdministradorRestaurante"));

        // Filtrar solo los usuarios de tipo "Cliente"
        List<Usuario> usuariosClientes = new ArrayList<>();
        for (Usuario usuario : usuarioList) {
            if ("Cliente".equals(usuario.getTipoUsuario())) {
                usuariosClientes.add(usuario);
            }
        }

        return usuariosClientes;
    }
}