package com.example.foodisea.activity.login;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodisea.R;
import com.example.foodisea.dialog.LoadingDialog;
import com.example.foodisea.databinding.ActivityForgotPasswordBinding;
import com.example.foodisea.repository.UsuarioRepository;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import java.util.concurrent.TimeUnit;

/**
 * Activity que maneja la recuperación de contraseña mediante correo electrónico
 * Incluye sistema de límite de intentos y validaciones de seguridad
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    /**
     * Constantes para manejo de datos y configuración
     */
    public static final String EXTRA_EMAIL = "extra_email";
    private static final int MAX_ATTEMPTS = 3;
    private static final long COOLDOWN_TIME = 300000;  // 5 minutos en milisegundos

    /**
     * ViewBinding para acceder a las vistas
     */
    private ActivityForgotPasswordBinding binding;

    /**
     * Repositorio para operaciones con usuarios
     */
    private UsuarioRepository usuarioRepository;

    /**
     * Diálogo de carga para operaciones asíncronas
     */
    private LoadingDialog loadingDialog;

    /**
     * Variables para control de intentos de recuperación
     */
    private int attemptCount = 0;
    private long lastAttemptTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeComponents();
        setupViews();
        handleIncomingEmail();
    }

    /**
     * Inicializa los componentes principales de la actividad
     */
    private void initializeComponents() {
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        usuarioRepository = new UsuarioRepository();
        loadingDialog = new LoadingDialog(this);

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
        setupHelperTexts();
    }

    /**
     * Configura los textos de ayuda en los campos
     */
    private void setupHelperTexts() {
        binding.etCorreoLayout.setHelperText("Recibirás un correo con instrucciones");
        binding.etCorreoLayout.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
        binding.etCorreoLayout.setEndIconDrawable(R.drawable.ic_email);
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
        binding.etCorreo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateEmail(s.toString());
                updateSendButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Maneja el email recibido desde la actividad de login si existe
     */
    private void handleIncomingEmail() {
        String email = getIntent().getStringExtra(EXTRA_EMAIL);
        if (email != null && !email.isEmpty()) {
            binding.etCorreo.setText(email);
            validateEmail(email);
            updateSendButtonState();
        }
    }

    /**
     * Actualiza el estado del botón de envío según la validación
     */
    private void updateSendButtonState() {
        binding.btnSentEmail.setEnabled(
                !binding.etCorreo.getText().toString().trim().isEmpty() &&
                        binding.etCorreoLayout.getError() == null
        );
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
        }
        binding.etCorreoLayout.setError(null);
        return true;
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

        // Verificar límite de intentos
        if (isAttemptLimitExceeded()) {
            showCooldownError();
            return;
        }

        loadingDialog.show(getString(R.string.mensaje_enviando_correo));

        usuarioRepository.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    loadingDialog.dismiss();
                    if (task.isSuccessful()) {
                        showSuccessAndFinish(getString(R.string.mensaje_recuperacion_enviado));
                    } else {
                        handleResetError(task.getException());
                    }
                    updateAttemptCount();
                });
    }

    /**
     * Verifica si se ha excedido el límite de intentos
     */
    private boolean isAttemptLimitExceeded() {
        long currentTime = System.currentTimeMillis();
        return currentTime - lastAttemptTime < COOLDOWN_TIME && attemptCount >= MAX_ATTEMPTS;
    }

    /**
     * Muestra el error de tiempo de espera
     */
    private void showCooldownError() {
        long currentTime = System.currentTimeMillis();
        long remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(
                COOLDOWN_TIME - (currentTime - lastAttemptTime)
        );
        String errorMessage = getString(R.string.error_demasiados_intentos, remainingMinutes);
        showError(errorMessage);
    }

    /**
     * Maneja los diferentes tipos de errores en el proceso de reset
     */
    private void handleResetError(Exception e) {
        String errorMessage;
        if (e instanceof FirebaseAuthInvalidUserException) {
            errorMessage = getString(R.string.error_usuario_no_existe);
        } else if (e instanceof FirebaseNetworkException) {
            errorMessage = getString(R.string.error_conexion);
        } else {
            errorMessage = getString(R.string.error_recuperacion_password);
            Log.e("ForgotPassword", "Error en recuperación de contraseña", e);
        }
        showError(errorMessage);
    }

    /**
     * Actualiza el contador de intentos y aplica el cooldown si es necesario
     */
    private void updateAttemptCount() {
        attemptCount++;
        lastAttemptTime = System.currentTimeMillis();

        if (attemptCount >= MAX_ATTEMPTS) {
            disableInputs();
            scheduleCooldownReset();
        }
    }

    /**
     * Deshabilita los inputs durante el cooldown
     */
    private void disableInputs() {
        binding.btnSentEmail.setEnabled(false);
        binding.etCorreo.setEnabled(false);
        binding.etCorreoLayout.setEndIconMode(TextInputLayout.END_ICON_NONE);
    }

    /**
     * Programa el reset del cooldown
     */
    private void scheduleCooldownReset() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isFinishing()) {
                binding.btnSentEmail.setEnabled(true);
                binding.etCorreo.setEnabled(true);
                binding.etCorreoLayout.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
                binding.etCorreoLayout.setEndIconDrawable(R.drawable.ic_email);
                attemptCount = 0;
            }
        }, COOLDOWN_TIME);
    }

    /**
     * Muestra un mensaje de éxito y finaliza la actividad
     */
    private void showSuccessAndFinish(String message) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.exito_titulo))
                .setMessage(message)
                .setPositiveButton(getString(R.string.aceptar), (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    /**
     * Muestra un diálogo de error
     */
    private void showError(String message) {
        if (!isFinishing()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.error_titulo))
                    .setMessage(message)
                    .setPositiveButton(getString(R.string.aceptar), null)
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