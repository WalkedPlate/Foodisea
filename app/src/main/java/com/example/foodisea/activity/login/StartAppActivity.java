package com.example.foodisea.activity.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodisea.MainActivity;
import com.example.foodisea.R;
import com.example.foodisea.activity.adminRes.AdminResHomeActivity;
import com.example.foodisea.activity.cliente.ClienteMainActivity;
import com.example.foodisea.activity.repartidor.RepartidorMainActivity;
import com.example.foodisea.activity.superadmin.SuperadminMainActivity;
import com.example.foodisea.data.SessionManager;
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
    private SessionManager sessionManager;

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
        sessionManager = SessionManager.getInstance(this);

        // Log del estado inicial
        Log.d("StartAppActivity", "IsLoggedIn: " + sessionManager.isLoggedIn());
        Log.d("StartAppActivity", "UserId: " + sessionManager.getUserId());
        if (sessionManager.getUsuarioActual() != null) {
            Log.d("StartAppActivity", "Usuario Actual Tipo: " + sessionManager.getUsuarioActual().getTipoUsuario());
        }
    }

    /**
     * Configura las vistas y sus listeners
     */
    private void setupViews() {
        binding.btnInicioSesion.setOnClickListener(v -> navigateToLogin());
        binding.btnRegistrate.setOnClickListener(v -> navigateToSelectRol());
    }

    /**
     * Verifica si existe una sesión activa usando SessionManager
     */
    private void checkExistingSession() {
        if (!sessionManager.isLoggedIn()) {
            Log.d("StartAppActivity", "No hay sesión activa");
            return;
        }

        Log.d("StartAppActivity", "Iniciando verificación de sesión");
        loadingDialog.show(getString(R.string.mensaje_cargando));

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            sessionManager.checkExistingSession(this, new SessionManager.SessionCallback() {
                @Override
                public void onSessionValid(Usuario usuario) {
                    Log.d("StartAppActivity", "Sesión válida para usuario: " + usuario.getId());
                    loadingDialog.dismiss();
                    redirectToUserScreen();
                }

                @Override
                public void onSessionError() {
                    Log.e("StartAppActivity", "Error en validación de sesión");
                    handleSessionError("Error al validar la sesión");
                }
            });
        } else {
            handleSessionError("No se encontró usuario autenticado");
        }
    }

    /**
     * Redirecciona al usuario a su pantalla correspondiente
     */
    private void redirectToUserScreen() {
        Intent intent = sessionManager.getRedirectIntent(this);
        if (intent != null) {
            Log.d("StartAppActivity", "Redirigiendo a: " + intent.getComponent().getClassName());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Log.e("StartAppActivity", "Intent de redirección es null");
            handleSessionError("Error al crear la redirección");
        }
    }

    /**
     * Maneja errores en la verificación de sesión
     */
    private void handleSessionError(String errorMessage) {
        if (!isFinishing()) {
            Log.e("StartAppActivity", "Error de sesión: " + errorMessage);
            loadingDialog.dismiss();
            sessionManager.logout();
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        }
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