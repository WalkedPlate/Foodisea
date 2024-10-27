package com.example.foodisea.activity.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodisea.R;
import com.example.foodisea.activity.dialog.LoadingDialog;
import com.example.foodisea.databinding.ActivityForgotPasswordBinding;
import com.example.foodisea.databinding.ActivityRegisterBinding;
import com.example.foodisea.repository.UsuarioRepository;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ForgotPasswordActivity extends AppCompatActivity {

    /**
     * ViewBinding para acceder a las vistas
     */
    ActivityForgotPasswordBinding binding;

    /**
     * Repositorio para operaciones con usuarios
     */
    private UsuarioRepository usuarioRepository;

    /**
     * Diálogo de carga para operaciones asíncronas
     */
    private LoadingDialog loadingDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeComponents();
        setupViews();
    }

    /**
     * Inicializa los componentes principales de la actividad
     */
    private void initializeComponents() {
        // Inicializar ViewBinding
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar repositorios y utilidades
        usuarioRepository = new UsuarioRepository();
        loadingDialog = new LoadingDialog(this);

        // Configurar diseño edge-to-edge
        EdgeToEdge.enable(this);
        setupWindowInsets();
    }

    /**
     * Configura los insets de la ventana para una correcta visualización
     */
    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Configura las vistas y listeners iniciales
     */
    private void setupViews() {
        setupClickListeners();
        setupInputValidation();
    }

    /**
     * Configura los listeners para todos los elementos interactivos
     */
    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSentEmail.setOnClickListener(v -> handleForgotPassword());
    }

    /**
     * Configura la validación en tiempo real de los campos
     */
    private void setupInputValidation() {
        // Validación del email
        binding.etCorreo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateEmail(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

    }



    /**
     * Valida el formato del email
     * @param email Email a validar
     * @return true si el email es válido
     */
    private boolean validateEmail(String email) {
        if (email.isEmpty()) {
            binding.etCorreoLayout.setError(getString(R.string.error_campo_requerido));
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etCorreoLayout.setError(getString(R.string.error_email_invalido));
            return false;
        } else {
            binding.etCorreoLayout.setError(null);
            return true;
        }
    }

    /**
     * Maneja el proceso de recuperación de contraseña
     */
    private void handleForgotPassword() {
        String email = binding.etCorreo.getText().toString().trim();
        if (!validateEmail(email)) {
            showError(getString(R.string.error_email_recuperacion));
            return;
        }

        loadingDialog.show();
        usuarioRepository.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    loadingDialog.dismiss();
                    if (task.isSuccessful()) {
                        showSuccess(getString(R.string.mensaje_recuperacion_enviado));
                    } else {
                        showError(getString(R.string.error_recuperacion_password));
                    }
                });
    }

    /**
     * Muestra un diálogo de error
     * @param message Mensaje de error a mostrar
     */
    private void showError(String message) {
        if (!isFinishing()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.error_titulo)
                    .setMessage(message)
                    .setPositiveButton(R.string.aceptar, null)
                    .show();
        }
    }

    /**
     * Muestra un diálogo de éxito
     * @param message Mensaje de éxito a mostrar
     */
    private void showSuccess(String message) {
        if (!isFinishing()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.exito_titulo)
                    .setMessage(message)
                    .setPositiveButton(R.string.aceptar, null)
                    .show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
        binding = null;
    }
}
