package com.example.foodisea.data;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.foodisea.activity.adminRes.AdminResHomeActivity;
import com.example.foodisea.activity.cliente.ClienteMainActivity;
import com.example.foodisea.activity.repartidor.RepartidorMainActivity;
import com.example.foodisea.activity.superadmin.SuperadminMainActivity;
import com.example.foodisea.model.AdministradorRestaurante;
import com.example.foodisea.model.Cliente;
import com.example.foodisea.model.Repartidor;
import com.example.foodisea.model.Superadmin;
import com.example.foodisea.model.Usuario;
import com.example.foodisea.repository.UsuarioRepository;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import java.util.Map;

/**
 * Gestor de sesión que maneja el estado del usuario actual en la aplicación.
 * Implementa el patrón Singleton para mantener una única instancia de la sesión
 * y centralizar el manejo de autenticación y datos del usuario.
 *
 * Características principales:
 * - Manejo de sesión persistente usando SharedPreferences
 * - Integración con Firebase Authentication
 * - Soporte para múltiples tipos de usuario
 * - Validación de sesiones automática
 * - Serialización/deserialización de datos del usuario
 */
public class SessionManager {
    private static SessionManager instance;
    private Usuario usuarioActual;
    private final SharedPreferences preferences;
    private final FirebaseAuth auth;
    private final UsuarioRepository usuarioRepository;

    // Constantes para SharedPreferences
    private static final String PREF_NAME = "LoginPrefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_TYPE = "user_type";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_NOMBRES = "nombres";
    private static final String KEY_APELLIDOS = "apellidos";
    private static final String KEY_CORREO = "correo";
    private static final String KEY_TELEFONO = "telefono";
    private static final String KEY_DIRECCION = "direccion";
    private static final String KEY_DOCUMENTO_ID = "documentoId";
    private static final String KEY_FECHA_NACIMIENTO = "fechaNacimiento";
    private static final String KEY_FOTO = "foto";
    private static final String KEY_ESTADO = "estado";
    private static final String KEY_RESTAURANTE_ID = "restauranteId";
    private static final String KEY_LATITUD = "latitud";
    private static final String KEY_LONGITUD = "longitud";

    /**
     * Constructor privado para implementar Singleton.
     */
    private SessionManager(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        auth = FirebaseAuth.getInstance();
        usuarioRepository = new UsuarioRepository();
        cargarUsuarioGuardado();
    }

    /**
     * Obtiene la instancia única del SessionManager.
     */
    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    /**
     * Carga el usuario guardado en SharedPreferences si existe
     */
    private void cargarUsuarioGuardado() {
        if (preferences.getBoolean(KEY_IS_LOGGED_IN, false)) {
            String tipo = preferences.getString(KEY_USER_TYPE, "");
            if (!tipo.isEmpty()) {
                try {
                    switch (tipo) {
                        case "Cliente":
                            usuarioActual = new Cliente();
                            break;
                        case "AdministradorRestaurante":
                            usuarioActual = new AdministradorRestaurante();
                            break;
                        case "Repartidor":
                            usuarioActual = new Repartidor();
                            break;
                        case "Superadmin":
                            usuarioActual = new Superadmin();
                            break;
                        default:
                            return;
                    }
                    cargarDatosUsuario(usuarioActual);
                } catch (Exception e) {
                    Log.e("SessionManager", "Error cargando usuario", e);
                    usuarioActual = null;
                }
            }
        }
    }

    /**
     * Carga los datos comunes y específicos del usuario
     */
    private void cargarDatosUsuario(Usuario usuario) {
        // Datos comunes
        usuario.setId(preferences.getString(KEY_USER_ID, ""));
        usuario.setNombres(preferences.getString(KEY_NOMBRES, ""));
        usuario.setApellidos(preferences.getString(KEY_APELLIDOS, ""));
        usuario.setCorreo(preferences.getString(KEY_CORREO, ""));
        usuario.setTelefono(preferences.getString(KEY_TELEFONO, ""));
        usuario.setDireccion(preferences.getString(KEY_DIRECCION, ""));
        usuario.setDocumentoId(preferences.getString(KEY_DOCUMENTO_ID, ""));
        usuario.setFechaNacimiento(preferences.getString(KEY_FECHA_NACIMIENTO, ""));
        usuario.setFoto(preferences.getString(KEY_FOTO, ""));
        usuario.setEstado(preferences.getString(KEY_ESTADO, ""));
        usuario.setTipoUsuario(preferences.getString(KEY_USER_TYPE, ""));

        // Datos específicos
        if (usuario instanceof AdministradorRestaurante) {
            ((AdministradorRestaurante) usuario).setRestauranteId(
                    preferences.getString(KEY_RESTAURANTE_ID, "")
            );
        } else if (usuario instanceof Repartidor) {
            Repartidor repartidor = (Repartidor) usuario;
            repartidor.setLatitud(preferences.getFloat(KEY_LATITUD, 0));
            repartidor.setLongitud(preferences.getFloat(KEY_LONGITUD, 0));
        }
    }

    /**
     * Verifica si existe una sesión activa y válida.
     */
    public void checkExistingSession(Context context, SessionCallback callback) {
        boolean isLoggedIn = preferences.getBoolean(KEY_IS_LOGGED_IN, false);
        if (!isLoggedIn) {
            callback.onSessionError();
            return;
        }

        String userId = preferences.getString(KEY_USER_ID, "");
        if (userId.isEmpty()) {
            callback.onSessionError();
            return;
        }

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null && currentUser.getUid().equals(userId)) {
            currentUser.reload()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            loadUserData(userId, callback);
                        } else {
                            callback.onSessionError();
                        }
                    });
        } else {
            callback.onSessionError();
        }
    }

    /**
     * Guarda la sesión del usuario actual
     */
    public void setUsuarioActual(Usuario usuario) {
        Log.d("SessionManager", "Guardando usuario: " + (usuario != null ? usuario.getId() : "null"));

        this.usuarioActual = usuario;
        if (usuario != null) {
            SharedPreferences.Editor editor = preferences.edit();

            // Guardar ID de Firebase Auth si está disponible
            FirebaseUser firebaseUser = auth.getCurrentUser();
            String userId = firebaseUser != null ? firebaseUser.getUid() : usuario.getId();

            Log.d("SessionManager", "Guardando userId: " + userId);

            // Datos comunes
            editor.putString(KEY_USER_ID, userId);
            editor.putString(KEY_USER_TYPE, usuario.getTipoUsuario());
            editor.putString(KEY_NOMBRES, usuario.getNombres());
            editor.putString(KEY_APELLIDOS, usuario.getApellidos());
            editor.putString(KEY_CORREO, usuario.getCorreo());
            editor.putString(KEY_TELEFONO, usuario.getTelefono());
            editor.putString(KEY_DIRECCION, usuario.getDireccion());
            editor.putString(KEY_DOCUMENTO_ID, usuario.getDocumentoId());
            editor.putString(KEY_FECHA_NACIMIENTO, usuario.getFechaNacimiento());
            editor.putString(KEY_FOTO, usuario.getFoto());
            editor.putString(KEY_ESTADO, usuario.getEstado());
            editor.putBoolean(KEY_IS_LOGGED_IN, true);

            // Datos específicos según tipo...

            boolean success = editor.commit();
            Log.d("SessionManager", "Guardado completado: " + success);
        }
    }

    /**
     * Carga los datos del usuario desde Firestore
     */
    private void loadUserData(String userId, SessionCallback callback) {
        Log.d("SessionManager", "Cargando datos de usuario: " + userId);

        usuarioRepository.getUserById(userId)
                .addOnSuccessListener(usuario -> {
                    Log.d("SessionManager", "Usuario cargado exitosamente");
                    if (validateUserByType(usuario)) {
                        usuario.setId(userId); // Asegurar que el ID está establecido
                        setUsuarioActual(usuario);
                        callback.onSessionValid(usuario);
                    } else {
                        Log.e("SessionManager", "Usuario no válido");
                        callback.onSessionError();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("SessionManager", "Error cargando usuario", e);
                    callback.onSessionError();
                });
    }

    /**
     * Valida el usuario según su tipo
     */
    public boolean validateUserByType(Usuario usuario) {
        if (usuario == null || usuario.getTipoUsuario() == null) {
            return false;
        }

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
            Log.e("SessionManager", "Error al validar usuario", e);
            return false;
        }
    }

    /**
     * Cierra la sesión actual
     */
    public void logout() {
        Log.d("SessionManager", "Cerrando sesión");
        usuarioActual = null;
        preferences.edit().clear().commit();
        auth.signOut();
    }

    /**
     * Obtiene el Intent apropiado para redirección
     */
    public Intent getRedirectIntent(Context context) {
        if (usuarioActual == null) return null;

        Intent intent;
        try {
            switch (usuarioActual.getTipoUsuario()) {
                case "AdministradorRestaurante":
                    intent = new Intent(context, AdminResHomeActivity.class);
                    AdministradorRestaurante admin = (AdministradorRestaurante) usuarioActual;
                    intent.putExtra("restauranteId", admin.getRestauranteId());
                    break;
                case "Repartidor":
                    intent = new Intent(context, RepartidorMainActivity.class);
                    Repartidor repartidor = (Repartidor) usuarioActual;
                    intent.putExtra("estadoRepartidor", repartidor.getEstado());
                    intent.putExtra("latitud", repartidor.getLatitud());
                    intent.putExtra("longitud", repartidor.getLongitud());
                    break;
                case "Cliente":
                    intent = new Intent(context, ClienteMainActivity.class);
                    break;
                case "Superadmin":
                    intent = new Intent(context, SuperadminMainActivity.class);
                    break;
                default:
                    return null;
            }

            intent.putExtra("userId", usuarioActual.getId());
            intent.putExtra("userType", usuarioActual.getTipoUsuario());
            intent.putExtra("nombres", usuarioActual.getNombres());
            intent.putExtra("apellidos", usuarioActual.getApellidos());

            return intent;
        } catch (ClassCastException e) {
            Log.e("SessionManager", "Error al crear intent de redirección", e);
            return null;
        }
    }

    // Getters para cada tipo de usuario
    public Cliente getClienteActual() {
        return usuarioActual instanceof Cliente ? (Cliente) usuarioActual : null;
    }

    public AdministradorRestaurante getAdminRestauranteActual() {
        return usuarioActual instanceof AdministradorRestaurante ?
                (AdministradorRestaurante) usuarioActual : null;
    }

    public Repartidor getRepartidorActual() {
        return usuarioActual instanceof Repartidor ?
                (Repartidor) usuarioActual : null;
    }

    public Superadmin getSuperadminActual() {
        return usuarioActual instanceof Superadmin ?
                (Superadmin) usuarioActual : null;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    /**
     * Verifica si hay una sesión activa
     */
    public boolean isLoggedIn() {
        boolean isLoggedIn = preferences.getBoolean(KEY_IS_LOGGED_IN, false);
        String userId = preferences.getString(KEY_USER_ID, "");
        FirebaseUser currentUser = auth.getCurrentUser();

        Log.d("SessionManager", "isLoggedIn check - SharedPrefs: " + isLoggedIn);
        Log.d("SessionManager", "isLoggedIn check - userId: " + userId);
        Log.d("SessionManager", "isLoggedIn check - FirebaseUser: " +
                (currentUser != null ? currentUser.getUid() : "null"));

        // Si hay usuario de Firebase pero no ID guardado, actualizar
        if (currentUser != null && userId.isEmpty()) {
            Log.d("SessionManager", "Actualizando userId con FirebaseUser");
            preferences.edit()
                    .putString(KEY_USER_ID, currentUser.getUid())
                    .commit();
            return true;
        }

        return isLoggedIn && currentUser != null;
    }

    public String getUserId() {
        return preferences.getString(KEY_USER_ID, "");
    }

    public Task<Usuario> loadUserData(String userId) {
        return usuarioRepository.getUserById(userId)
                .addOnSuccessListener(this::setUsuarioActual);
    }

    public interface SessionCallback {
        void onSessionValid(Usuario usuario);
        void onSessionError();
    }
}