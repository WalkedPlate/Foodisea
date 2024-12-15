package com.example.foodisea.activity.superadmin;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.foodisea.R;
import com.example.foodisea.databinding.ActivitySuperAdminDetSoliRepartidorBinding;
import com.example.foodisea.databinding.ActivitySuperAdminDetalleUsuarioBinding;
import com.example.foodisea.model.Usuario;

public class SuperAdminDetalleUsuarioActivity extends AppCompatActivity {

    ActivitySuperAdminDetalleUsuarioBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeComponents();
        setupClick();

        // Obtener usuario desde el intent
        Usuario usuario = (Usuario) getIntent().getSerializableExtra("usuario");
        if (usuario != null) {
            cargarDatosUsuario(usuario);
        }
    }

    private void initializeComponents() {
        binding = ActivitySuperAdminDetalleUsuarioBinding.inflate(getLayoutInflater());
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

        if(usuario.getTipoUsuario().equals("AdministradorRestaurante")){
            binding.tvTypeUser.setText("Administrador de restaurante");
        } else {
            binding.tvTypeUser.setText(usuario.getTipoUsuario());
        }

        binding.tvEstadoUser.setText(usuario.getEstado());

        if(usuario.getEstado().equals("Activo")){
            binding.tvEstadoUser.setTextColor(ContextCompat.getColor(this, R.color.success));
        } else{
            binding.tvEstadoUser.setTextColor(ContextCompat.getColor(this, R.color.crimson));
        }

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
                    .placeholder(R.drawable.ic_usuarios)
                    .error(R.drawable.ic_usuarios)
                    .circleCrop()
                    .into(binding.ivUserPhoto);
        } else {
            Glide.with(this)
                    .load(R.drawable.ic_usuarios)
                    .circleCrop()
                    .into(binding.ivUserPhoto);
        }
    }
}