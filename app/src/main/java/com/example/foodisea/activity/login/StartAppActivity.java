package com.example.foodisea.activity.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodisea.MainActivity;
import com.example.foodisea.R;
import com.example.foodisea.activity.adminRes.AdminResHomeActivity;
import com.example.foodisea.activity.cliente.ClienteMainActivity;
import com.example.foodisea.activity.repartidor.RepartidorMainActivity;
import com.example.foodisea.activity.superadmin.SuperadminMainActivity;
import com.example.foodisea.databinding.ActivityStartAppBinding;
import com.example.foodisea.dialog.LoadingDialog;
import com.example.foodisea.model.AdministradorRestaurante;
import com.example.foodisea.model.Repartidor;
import com.example.foodisea.model.Usuario;
import com.example.foodisea.repository.UsuarioRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Activity inicial que maneja el flujo de entrada a la aplicación
 * y la verificación de sesión automática
 */
public class StartAppActivity extends AppCompatActivity {

    private ActivityStartAppBinding binding;
    private LoadingDialog loadingDialog;
    private SharedPreferences prefs;
    private UsuarioRepository usuarioRepository;

    // Constantes para SharedPreferences
    private static final String PREF_NAME = "LoginPrefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_TYPE = "user_type";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeComponents();
        setupViews();
        checkExistingSession();
    }

    /**
     * Inicializa los componentes principales
     */
    private void initializeComponents() {
        binding = ActivityStartAppBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);

        loadingDialog = new LoadingDialog(this);
        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        usuarioRepository = new UsuarioRepository();
    }

    /**
     * Configura las vistas y sus listeners
     */
    private void setupViews() {
        binding.btnInicioSesion.setOnClickListener(v -> navigateToLogin());
        binding.btnRegistrate.setOnClickListener(v -> navigateToSelectRol());
    }

    /**
     * Verifica si existe una sesión activa
     */
    private void checkExistingSession() {
        boolean isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);
        if (!isLoggedIn) return;

        String userId = prefs.getString(KEY_USER_ID, "");
        if (userId.isEmpty()) return;

        // Mostrar loading mientras se verifica
        loadingDialog.show(getString(R.string.mensaje_cargando));

        // Verificar usuario en Firebase
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getUid().equals(userId)) {
            // Verificar token y estado
            currentUser.reload()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Token válido, cargar datos del usuario
                            loadUserData(userId);
                        } else {
                            // Token inválido
                            handleSessionError();
                        }
                    });
        } else {
            handleSessionError();
        }
    }

    /**
     * Carga los datos del usuario y redirecciona según su tipo
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
     * Valida el usuario según su tipo
     */
    private boolean validateUserByType(Usuario usuario) {
        if (usuario == null || usuario.getTipoUsuario() == null) return false;

        if (!"Activo".equals(usuario.getEstado())) {
            return false;
        }

        try {
            switch (usuario.getTipoUsuario()) {
                case "AdministradorRestaurante":
                    AdministradorRestaurante admin = (AdministradorRestaurante) usuario;
                    return admin.getRestauranteId() != null && !admin.getRestauranteId().isEmpty();
                case "Repartidor":
                    Repartidor repartidor = (Repartidor) usuario;
                    return !"Ocupado".equals(repartidor.getEstado());
                case "Cliente":
                case "Superadmin":
                    return true;
                default:
                    return false;
            }
        } catch (ClassCastException e) {
            return false;
        }
    }

    /**
     * Redirecciona al usuario según su tipo
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
                    return;
            }

            // Datos comunes
            intent.putExtra("userId", usuario.getId());
            intent.putExtra("userType", usuario.getTipoUsuario());
            intent.putExtra("nombres", usuario.getNombres());
            intent.putExtra("apellidos", usuario.getApellidos());

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

        } catch (ClassCastException e) {
            handleSessionError();
        }
    }

    /**
     * Maneja errores en la verificación de sesión
     */
    private void handleSessionError() {
        loadingDialog.dismiss();
        clearUserSession();
    }

    /**
     * Limpia los datos de sesión
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
     * Navega a la pantalla de login
     */
    private void navigateToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
    }

    /**
     * Navega a la pantalla de selección de rol
     */
    private void navigateToSelectRol() {
        startActivity(new Intent(this, SelectRolActivity.class));
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