package com.example.foodisea.activity.superadmin;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.foodisea.R;
import com.example.foodisea.databinding.ActivitySuperAdminDetSoliRepartidorBinding;
import com.example.foodisea.model.Usuario;
import com.example.foodisea.repository.UsuarioRepository;

public class SuperAdminDetSoliRepartidorActivity extends AppCompatActivity {

    ActivitySuperAdminDetSoliRepartidorBinding binding;
    UsuarioRepository usuarioRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeComponents();
        setupClick();

        usuarioRepository = new UsuarioRepository();

        // Obtener usuario desde el intent
        Usuario usuario = (Usuario) getIntent().getSerializableExtra("usuario");
        if (usuario != null) {
            cargarDatosUsuario(usuario);
            setupListeners(usuario);
        }
    }

    private void initializeComponents() {
        binding = ActivitySuperAdminDetSoliRepartidorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupWindowInsets();
    }

    private void setupWindowInsets() {
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupClick() {
        binding.btnBack.setOnClickListener(v -> finish());
    }

    // Cargar datos en la vista
    private void cargarDatosUsuario(Usuario usuario) {
        binding.tvUserName.setText(usuario.getNombres() + " " + usuario.getApellidos());
        binding.tvTipoDocId.setText(usuario.getTipoDocumentoId());
        binding.tvUserDocId.setText(usuario.getDocumentoId());
        binding.tvUserBirthdate.setText(usuario.getFechaNacimiento());
        binding.tvUserMail.setText(usuario.getCorreo());
        binding.tvUserCell.setText(usuario.getTelefono());
        binding.tvUserAddress.setText(usuario.getDireccion());

        if(usuario.getFoto() != null && !usuario.getFoto().isEmpty()){
            Glide.with(this)
                    .load(usuario.getFoto())
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

    // Configurar botones de aceptar y denegar
    private void setupListeners(Usuario usuario) {
        binding.btnAccept.setOnClickListener(v -> actualizarEstado(usuario, "Activo"));
        binding.btnDeny.setOnClickListener(v -> actualizarEstado(usuario, "Denegado"));
    }

    // Actualizar el estado del usuario
    private void actualizarEstado(Usuario usuario, String nuevoEstado) {
        usuario.setEstado(nuevoEstado);
        usuarioRepository.actualizarEstadoUsuario(usuario, nuevoEstado)
                .addOnSuccessListener(aVoid -> {
                    if(nuevoEstado.equals("Activo")){
                        Toast.makeText(this, "El usuario ha sido aceptado" , Toast.LENGTH_SHORT).show();

                    } else if (nuevoEstado.equals("Denegado")) {
                        Toast.makeText(this, "El usuario ha sido denegado", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}