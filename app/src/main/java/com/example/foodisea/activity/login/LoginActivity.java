package com.example.foodisea.activity.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodisea.R;
import com.example.foodisea.activity.adminRes.AdminResHomeActivity;
import com.example.foodisea.activity.cliente.ClienteMainActivity;
import com.example.foodisea.dialog.LoadingDialog;
import com.example.foodisea.activity.repartidor.RepartidorMainActivity;
import com.example.foodisea.activity.superadmin.SuperadminMainActivity;
import com.example.foodisea.databinding.ActivityLoginBinding;
import com.example.foodisea.model.AdministradorRestaurante;
import com.example.foodisea.model.Repartidor;
import com.example.foodisea.model.Usuario;
import com.example.foodisea.repository.UsuarioRepository;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

/**
 * Activity que maneja el proceso de inicio de sesión de usuarios.
 * Soporta múltiples tipos de usuario mediante herencia:
 * - Cliente
 * - AdministradorRestaurante
 * - Repartidor
 * - Superadmin
 *
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

    /**
     * Constantes para SharedPreferences
     */
    private static final String PREF_NAME = "LoginPrefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_TYPE = "user_type";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_LAST_ACCESS = "last_access";

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
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        usuarioRepository = new UsuarioRepository();
        loadingDialog = new LoadingDialog(this);
        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

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
        binding.btnEntrarApp.setOnClickListener(v -> attemptLogin());
        binding.btnBack.setOnClickListener(v -> finish());
        binding.tvForgotPassword.setOnClickListener(v -> navigateToForgotPassword());
        binding.tvRegistrate.setOnClickListener(v -> navigateToSelectRol());
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
                updateLoginButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword(s.toString());
                updateLoginButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Actualiza el estado del botón de login según la validación de campos
     */
    private void updateLoginButtonState() {
        boolean isValid = validateEmail(binding.etCorreo.getText().toString()) &&
                validatePassword(binding.etPassword.getText().toString());
        binding.btnEntrarApp.setEnabled(isValid);
        binding.btnEntrarApp.setAlpha(isValid ? 1.0f : 0.6f);
    }

    /**
     * Carga los datos del usuario desde Firestore
     */
    private void loadUserData(String userId) {
        usuarioRepository.getUserById(userId)
                .addOnSuccessListener(usuario -> {
                    loadingDialog.dismiss();
                    if (validateUserByType(usuario)) {
                        redirectBasedOnUserType(usuario);
                    } else {
                        handleSessionError();
                    }
                })
                .addOnFailureListener(e -> handleSessionError());
    }

    /**
     * Maneja errores en la restauración de sesión
     */
    private void handleSessionError() {
        loadingDialog.dismiss();
        clearUserSession();
        Snackbar.make(binding.getRoot(),
                "Se cerró tu sesión anterior por seguridad",
                Snackbar.LENGTH_SHORT).show();
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
     */
    private void handleLoginError(Exception e) {
        loadingDialog.dismiss();
        String errorMessage = getAuthErrorMessage(e);
        showError(errorMessage);
    }

    /**
     * Obtiene el mensaje de error específico según la excepción
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
     * Guarda los datos de sesión del usuario
     */
    private void saveUserSession(Usuario usuario) {
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(KEY_USER_ID, usuario.getId());
        editor.putString(KEY_USER_TYPE, usuario.getTipoUsuario());
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putLong(KEY_LAST_ACCESS, System.currentTimeMillis());

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
     * Navega a la pantalla de selección de rol para registro
     */
    private void navigateToSelectRol() {
        Intent intent = new Intent(this, SelectRolActivity.class);
        startActivity(intent);
    }

    /**
     * Navega a la pantalla de recuperación de contraseña
     */
    private void navigateToForgotPassword() {
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        String currentEmail = binding.etCorreo.getText().toString().trim();
        if (!currentEmail.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(currentEmail).matches()) {
            intent.putExtra(ForgotPasswordActivity.EXTRA_EMAIL, currentEmail);
        }
        startActivity(intent);
    }

    /**
     * Valida todos los campos del formulario
     */
    private boolean validateFields() {
        String email = binding.etCorreo.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        return validateEmail(email) & validatePassword(password);
    }

    /**
     * Valida el formato del email
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
        binding = null;
    }
}