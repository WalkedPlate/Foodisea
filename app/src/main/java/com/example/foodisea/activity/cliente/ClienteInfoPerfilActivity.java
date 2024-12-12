package com.example.foodisea.activity.cliente;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.foodisea.R;
import com.example.foodisea.data.SessionManager;
import com.example.foodisea.databinding.ActivityClienteInfoPerfilBinding;
import com.example.foodisea.databinding.ActivityClientePerfilBinding;
import com.example.foodisea.model.Cliente;

public class ClienteInfoPerfilActivity extends AppCompatActivity {

    ActivityClienteInfoPerfilBinding binding;
    private SessionManager sessionManager;
    private Cliente clienteActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeComponents();
        setupListeners();

    }

    /**
     * Inicializa los componentes principales de la actividad
     */
    private void initializeComponents() {
        binding = ActivityClienteInfoPerfilBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar SessionManager
        sessionManager = SessionManager.getInstance(this);

        // Obtener al cliente logueado
        clienteActual = sessionManager.getClienteActual();

        // Actualizar la UI con los datos del cliente
        updateUIWithUserData();

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
        binding.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, ClienteEditarPerfilActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Actualiza la UI con los datos del cliente actual
     */
    private void updateUIWithUserData() {
        if (clienteActual != null) {
            // Actualizar datos del cliente
            binding.tvTipoDocId.setText(clienteActual.getTipoDocumentoId());
            binding.tvUserName.setText(clienteActual.obtenerNombreCompleto());
            binding.tvUserDocId.setText(clienteActual.getDocumentoId());
            binding.tvUserBirthdate.setText(clienteActual.getFechaNacimiento());
            binding.tvUserMail.setText(clienteActual.getCorreo());
            binding.tvUserCell.setText(clienteActual.getTelefono());
            binding.tvUserAddress.setText(clienteActual.getDireccion());

            // Cargar imagen de perfil
            if (clienteActual.getFoto() != null && !clienteActual.getFoto().isEmpty()) {
                // Cargar imagen desde URL
                Glide.with(this)
                        .load(clienteActual.getFoto())
                        .placeholder(R.drawable.ic_profile) // Imagen por defecto mientras carga
                        .error(R.drawable.error_image)      // Imagen si hay error al cargar
                        .circleCrop()
                        .into(binding.ivUserPhoto);
            } else {
                // Si no hay foto, mostrar imagen por defecto
                Glide.with(this)
                        .load(R.drawable.ic_profile)
                        .circleCrop()
                        .into(binding.ivUserPhoto);
            }

            // Mostrar tipo de usuario
            binding.tvTypeUser.setText("Cliente de Foodisea");
        }
    }
}