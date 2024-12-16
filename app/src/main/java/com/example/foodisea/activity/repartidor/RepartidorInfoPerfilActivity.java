package com.example.foodisea.activity.repartidor;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.foodisea.R;
import com.example.foodisea.manager.SessionManager;
import com.example.foodisea.databinding.ActivityRepartidorInfoPerfilBinding;
import com.example.foodisea.model.Repartidor;

public class RepartidorInfoPerfilActivity extends AppCompatActivity {

    ActivityRepartidorInfoPerfilBinding binding;
    private SessionManager sessionManager;
    private Repartidor repartidorActual;

    // Declaración del ActivityResultLauncher
    private final ActivityResultLauncher<Intent> editProfileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    // Obtener datos enviados desde la actividad de edición
                    String telefono = result.getData().getStringExtra("telefono");
                    String direccion = result.getData().getStringExtra("direccion");
                    String foto = result.getData().getStringExtra("foto");

                    // Actualizar datos del cliente actual
                    repartidorActual.setTelefono(telefono);
                    repartidorActual.setDireccion(direccion);
                    repartidorActual.setFoto(foto);

                    // Actualizar UI
                    updateUIWithUserData();
                }
            }
    );

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
        binding = ActivityRepartidorInfoPerfilBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar SessionManager
        sessionManager = SessionManager.getInstance(this);

        // Obtener al administrador de restaurante logueado
        repartidorActual = sessionManager.getRepartidorActual();

        // Actualizar la UI con los datos del administrador
        updateUIWithUserData();

        EdgeToEdge.enable(this);
        setupWindowInsets();
    }

    /**
     * Actualiza la UI con los datos del repartidor actual
     */
    private void updateUIWithUserData() {
        if (repartidorActual != null) {
            // Actualizar los datos del repartidor
            binding.tvTipoDocId.setText(repartidorActual.getTipoDocumentoId());
            binding.tvUserName.setText(repartidorActual.obtenerNombreCompleto());
            binding.tvUserDocId.setText(repartidorActual.getDocumentoId());
            binding.tvUserBirthdate.setText(repartidorActual.getFechaNacimiento());
            binding.tvUserMail.setText(repartidorActual.getCorreo());
            binding.tvUserCell.setText(repartidorActual.getTelefono());
            binding.tvUserAddress.setText(repartidorActual.getDireccion());


            // Cargar imagen de perfil
            if (repartidorActual.getFoto() != null && !repartidorActual.getFoto().isEmpty()) {
                Glide.with(this)
                        .load(repartidorActual.getFoto())
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.error_image)
                        .circleCrop()
                        .into(binding.ivUserPhoto);
            } else {
                Glide.with(this)
                        .load(R.drawable.ic_profile)
                        .circleCrop()
                        .into(binding.ivUserPhoto);
            }

            // Mostrar tipo de usuario
            binding.tvTypeUser.setText("Repartidor de Foodisea");
        }
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
            Intent intent = new Intent(this, RepartidorEditarPerfilActivity.class);
            intent.putExtra("telefono", repartidorActual.getTelefono());
            intent.putExtra("direccion", repartidorActual.getDireccion());
            intent.putExtra("foto", repartidorActual.getFoto());
            editProfileLauncher.launch(intent);  // Lanzar actividad de edición
        });
    }
}