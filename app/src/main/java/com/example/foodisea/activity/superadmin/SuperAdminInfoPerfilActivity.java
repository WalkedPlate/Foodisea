package com.example.foodisea.activity.superadmin;

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
import com.example.foodisea.databinding.ActivitySuperAdminInfoPerfilBinding;
import com.example.foodisea.model.Superadmin;

public class SuperAdminInfoPerfilActivity extends AppCompatActivity {

    ActivitySuperAdminInfoPerfilBinding binding;
    private SessionManager sessionManager;
    private Superadmin superadminActual;

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
                    superadminActual.setTelefono(telefono);
                    superadminActual.setDireccion(direccion);
                    superadminActual.setFoto(foto);

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
        binding = ActivitySuperAdminInfoPerfilBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar SessionManager
        sessionManager = SessionManager.getInstance(this);

        // Obtener al administrador de restaurante logueado
        superadminActual = sessionManager.getSuperadminActual();

        // Actualizar la UI con los datos del super administrador
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
            Intent intent = new Intent(this, SuperAdminEditarPerfilActivity.class);
            intent.putExtra("telefono", superadminActual.getTelefono());
            intent.putExtra("direccion", superadminActual.getDireccion());
            intent.putExtra("foto", superadminActual.getFoto());
            editProfileLauncher.launch(intent);  // Lanzar actividad de edición
            startActivity(intent);
        });
    }

    /**
     * Actualiza la UI con los datos del superadmin actual
     */
    private void updateUIWithUserData() {
        if (superadminActual != null) {
            // Actualizar datos del superadmin
            binding.tvUserName.setText(superadminActual.obtenerNombreCompleto());
            binding.tvUserDNI.setText(superadminActual.getDocumentoId());
            binding.tvUserBirthdate.setText(superadminActual.getFechaNacimiento());
            binding.tvUserMail.setText(superadminActual.getCorreo());
            binding.tvUserAddress.setText(superadminActual.getDireccion());

            // Cargar imagen de perfil
            if (superadminActual.getFoto() != null && !superadminActual.getFoto().isEmpty()) {
                Glide.with(this)
                        .load(superadminActual.getFoto())
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
        }
    }

}