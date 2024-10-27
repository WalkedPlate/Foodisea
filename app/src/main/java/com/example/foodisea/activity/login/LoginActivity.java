package com.example.foodisea.activity.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodisea.MainActivity;
import com.example.foodisea.R;
import com.example.foodisea.activity.adminRes.AdminResHomeActivity;
import com.example.foodisea.activity.cliente.ClienteMainActivity;
import com.example.foodisea.activity.dialog.LoadingDialog;
import com.example.foodisea.activity.repartidor.RepartidorMainActivity;
import com.example.foodisea.activity.superadmin.SuperadminMainActivity;
import com.example.foodisea.databinding.ActivityLoginBinding;
import com.example.foodisea.model.AdministradorRestaurante;
import com.example.foodisea.model.Repartidor;
import com.example.foodisea.model.Usuario;
import com.example.foodisea.repository.UsuarioRepository;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

/**
 * Activity que maneja el proceso de inicio de sesión de usuarios.
 * Soporta múltiples tipos de usuario mediante herencia:
 * - Cliente
 * - AdministradorRestaurante
 * - Repartidor
 * - Superadmin
 *
 * @author WalkedPlate
 * @version 1.0
 * @see Usuario
 * @see UsuarioRepository
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * ViewBinding para acceder a las vistas
     */
    private ActivityLoginBinding binding;

    /**
     * Repositorio para operaciones con usuarios
     */
    private UsuarioRepository usuarioRepository;

    /**
     * Diálogo de carga para operaciones asíncronas
     */
    private LoadingDialog loadingDialog;

    /**
     * SharedPreferences para almacenamiento local
     */
    private SharedPreferences prefs;

    // Constantes para SharedPreferences
    private static final String PREF_NAME = "LoginPrefs";
    private static final String KEY_REMEMBER_EMAIL = "remember_email";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_TYPE = "user_type";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeComponents();
        setupViews();
        checkPreviousSession();
    }

    /**
     * Inicializa los componentes principales de la actividad
     */
    private void initializeComponents() {
        // Inicializar ViewBinding
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar repositorios y utilidades
        usuarioRepository = new UsuarioRepository();
        loadingDialog = new LoadingDialog(this);
        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

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
        setupRememberEmail();
        setupInputValidation();
    }

    /**
     * Configura los listeners para todos los elementos interactivos
     */
    private void setupClickListeners() {
        binding.btnEntrarApp.setOnClickListener(v -> attemptLogin());
        binding.btnBack.setOnClickListener(v -> finish());
        binding.tvForgotPassword.setOnClickListener(v -> handleForgotPassword());
        binding.tvRegistrate.setOnClickListener(v -> navigateToRegister());
    }

    /**
     * Configura la funcionalidad de recordar email
     */
    private void setupRememberEmail() {
        boolean rememberEmailEnabled = prefs.getBoolean(KEY_REMEMBER_EMAIL, false);
        String savedEmail = prefs.getString(KEY_EMAIL, "");

        binding.cbRememberMe.setChecked(rememberEmailEnabled);
        if (rememberEmailEnabled && !savedEmail.isEmpty()) {
            binding.etCorreo.setText(savedEmail);
        }

        binding.cbRememberMe.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                clearSavedEmail();
            } else {
                String currentEmail = binding.etCorreo.getText().toString().trim();
                if (!currentEmail.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(currentEmail).matches()) {
                    saveEmail(currentEmail);
                }
            }
        });
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
                updateRememberEmail(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Validación de la contraseña
        binding.etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Actualiza el email guardado si la opción está habilitada
     * @param email Email actual
     */
    private void updateRememberEmail(String email) {
        if (binding.cbRememberMe.isChecked()) {
            if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                saveEmail(email);
            }
        }
    }

    /**
     * Verifica si existe una sesión previa válida
     */
    private void checkPreviousSession() {
        boolean isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);
        if (isLoggedIn) {
            String userId = prefs.getString(KEY_USER_ID, "");
            if (!userId.isEmpty()) {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null && currentUser.getUid().equals(userId)) {
                    usuarioRepository.getUserById(userId)
                            .addOnSuccessListener(usuario -> {
                                if (validateUserByType(usuario)) {
                                    redirectBasedOnUserType(usuario);
                                } else {
                                    clearUserSession();
                                }
                            })
                            .addOnFailureListener(e -> clearUserSession());
                } else {
                    clearUserSession();
                }
            }
        }
    }

    /**
     * Intenta realizar el login con las credenciales proporcionadas
     */
    private void attemptLogin() {
        if (!validateFields()) {
            return;
        }

        String email = binding.etCorreo.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        loadingDialog.show(getString(R.string.mensaje_iniciando_sesion));

        usuarioRepository.loginUser(email, password)
                .addOnSuccessListener(this::handleLoginSuccess)
                .addOnFailureListener(this::handleLoginError);
    }

    /**
     * Valida el usuario según su tipo específico
     * @param usuario Usuario a validar
     * @return true si el usuario es válido según su tipo
     */
    private boolean validateUserByType(Usuario usuario) {
        if (usuario == null || usuario.getTipoUsuario() == null) {
            showError("Error: Usuario o tipo de usuario no definido");
            return false;
        }

        if (!"Activo".equals(usuario.getEstado())) {
            if ("Repartidor".equals(usuario.getTipoUsuario())) {
                showError("Tu cuenta de repartidor está pendiente de aprobación");
            } else {
                showError("Tu cuenta no está activa. Contacta al administrador.");
            }
            return false;
        }

        try {
            switch (usuario.getTipoUsuario()) {
                case "AdministradorRestaurante":
                    AdministradorRestaurante admin = (AdministradorRestaurante) usuario;
                    if (admin.getRestauranteId() == null || admin.getRestauranteId().isEmpty()) {
                        showError("Error: Administrador sin restaurante asignado");
                        return false;
                    }
                    break;
                case "Repartidor":
                    Repartidor repartidor = (Repartidor) usuario;
                    if ("Ocupado".equals(repartidor.getEstado())) {
                        showError("No puedes iniciar sesión mientras estás en un pedido");
                        return false;
                    }
                    break;
                case "Cliente":
                case "Superadmin":
                    // No requieren validaciones adicionales
                    break;
                default:
                    showError("Tipo de usuario no válido");
                    return false;
            }
        } catch (ClassCastException e) {
            showError("Error en los datos del usuario");
            return false;
        }

        return true;
    }

    /**
     * Maneja el caso de login exitoso
     * @param usuario Usuario autenticado
     */
    private void handleLoginSuccess(Usuario usuario) {
        loadingDialog.dismiss();

        if (!validateUserByType(usuario)) {
            FirebaseAuth.getInstance().signOut();
            return;
        }

        saveUserSession(usuario);
        redirectBasedOnUserType(usuario);
    }

    /**
     * Maneja los errores durante el proceso de login
     * @param e Excepción producida
     */
    private void handleLoginError(Exception e) {
        loadingDialog.dismiss();
        String errorMessage = getAuthErrorMessage(e);
        showError(errorMessage);
    }

    /**
     * Obtiene el mensaje de error específico según la excepción
     * @param e Excepción de autenticación
     * @return Mensaje de error formateado
     */
    private String getAuthErrorMessage(Exception e) {
        if (e instanceof FirebaseAuthInvalidUserException) {
            return getString(R.string.error_usuario_no_existe);
        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
            return getString(R.string.error_credenciales_invalidas);
        } else if (e instanceof FirebaseNetworkException) {
            return getString(R.string.error_conexion);
        }
        return getString(R.string.error_generico_login);
    }

    /**
     * Guarda los datos de sesión específicos según el tipo de usuario
     * @param usuario Usuario a guardar en sesión
     */
    private void saveUserSession(Usuario usuario) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USER_ID, usuario.getId());
        editor.putString(KEY_USER_TYPE, usuario.getTipoUsuario());
        editor.putBoolean(KEY_IS_LOGGED_IN, true);

        try {
            switch (usuario.getTipoUsuario()) {
                case "AdministradorRestaurante":
                    AdministradorRestaurante admin = (AdministradorRestaurante) usuario;
                    editor.putString("restauranteId", admin.getRestauranteId());
                    break;
                case "Repartidor":
                    Repartidor repartidor = (Repartidor) usuario;
                    editor.putString("estadoRepartidor", repartidor.getEstado());
                    editor.putFloat("latitud", (float) repartidor.getLatitud());
                    editor.putFloat("longitud", (float) repartidor.getLongitud());
                    break;
            }
        } catch (ClassCastException e) {
            Log.e("LoginActivity", "Error al guardar datos específicos del usuario", e);
        }

        editor.apply();
    }

    /**
     * Redirige al usuario a su actividad correspondiente según su tipo
     * @param usuario Usuario a redireccionar
     */
    private void redirectBasedOnUserType(Usuario usuario) {
        Intent intent;
        try {
            switch (usuario.getTipoUsuario()) {
                case "AdministradorRestaurante":
                    intent = new Intent(this, AdminResHomeActivity.class);
                    AdministradorRestaurante admin = (AdministradorRestaurante) usuario;
                    intent.putExtra("restauranteId", admin.getRestauranteId());
                    break;
                case "Repartidor":
                    intent = new Intent(this, RepartidorMainActivity.class);
                    Repartidor repartidor = (Repartidor) usuario;
                    intent.putExtra("estadoRepartidor", repartidor.getEstado());
                    intent.putExtra("latitud", repartidor.getLatitud());
                    intent.putExtra("longitud", repartidor.getLongitud());
                    break;
                case "Cliente":
                    intent = new Intent(this, ClienteMainActivity.class);
                    break;
                case "Superadmin":
                    intent = new Intent(this, SuperadminMainActivity.class);
                    break;
                default:
                    showError("Tipo de usuario no válido");
                    return;
            }

            // Datos comunes para todos los tipos
            intent.putExtra("userId", usuario.getId());
            intent.putExtra("userType", usuario.getTipoUsuario());
            intent.putExtra("nombres", usuario.getNombres());
            intent.putExtra("apellidos", usuario.getApellidos());

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

        } catch (ClassCastException e) {
            Log.e("LoginActivity", "Error al redireccionar usuario", e);
            showError("Error al procesar los datos del usuario");
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
     * Navega a la pantalla de registro
     */
    private void navigateToRegister() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    /**
     * Valida todos los campos del formulario
     * @return true si todos los campos son válidos
     */
    private boolean validateFields() {
        String email = binding.etCorreo.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        return validateEmail(email) & validatePassword(password);
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
     * Valida el formato y longitud de la contraseña
     * @param password Contraseña a validar
     * @return true si la contraseña es válida
     */
    private boolean validatePassword(String password) {
        if (password.isEmpty()) {
            binding.etPasswordLayout.setError(getString(R.string.error_campo_requerido));
            return false;
        } else if (password.length() < 6) {
            binding.etPasswordLayout.setError(getString(R.string.error_password_corto));
            return false;
        } else {
            binding.etPasswordLayout.setError(null);
            return true;
        }
    }

    /**
     * Guarda el email en las preferencias si la opción está habilitada
     * @param email Email a guardar
     */
    private void saveEmail(String email) {
        if (email != null && !email.isEmpty()) {
            prefs.edit()
                    .putString(KEY_EMAIL, email)
                    .putBoolean(KEY_REMEMBER_EMAIL, true)
                    .apply();
        }
    }

    /**
     * Limpia el email guardado en las preferencias
     */
    private void clearSavedEmail() {
        prefs.edit()
                .remove(KEY_EMAIL)
                .putBoolean(KEY_REMEMBER_EMAIL, false)
                .apply();
    }

    /**
     * Limpia todos los datos de sesión del usuario
     */
    private void clearUserSession() {
        prefs.edit()
                .remove(KEY_USER_ID)
                .remove(KEY_USER_TYPE)
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .remove("restauranteId")
                .remove("estadoRepartidor")
                .remove("latitud")
                .remove("longitud")
                .apply();

        FirebaseAuth.getInstance().signOut();
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