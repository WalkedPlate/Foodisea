package com.example.foodisea.manager;

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
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
            // Convertir de String a double
            try {
                String latStr = preferences.getString(KEY_LATITUD, "0.0");
                String lonStr = preferences.getString(KEY_LONGITUD, "0.0");
                repartidor.setLatitud(Double.parseDouble(latStr));
                repartidor.setLongitud(Double.parseDouble(lonStr));
            } catch (NumberFormatException e) {
                Log.e("SessionManager", "Error al convertir coordenadas", e);
                repartidor.setLatitud(0.0);
                repartidor.setLongitud(0.0);
            }
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

        // Verificar que el usuario esté autenticado en Firebase
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            currentUser.reload()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Cargar datos usando el ID de Firestore guardado
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

            // Guardar el ID del documento de Firestore
            String userId = usuario.getId(); // Usamos directamente el ID del documento
            Log.d("SessionManager", "Guardando userId de Firestore: " + userId);

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

            // Datos específicos según tipo
            if (usuario instanceof AdministradorRestaurante) {
                AdministradorRestaurante admin = (AdministradorRestaurante) usuario;
                editor.putString(KEY_RESTAURANTE_ID, admin.getRestauranteId());
            } else if (usuario instanceof Repartidor) {
                Repartidor repartidor = (Repartidor) usuario;
                editor.putString(KEY_LATITUD, String.valueOf(repartidor.getLatitud()));
                editor.putString(KEY_LONGITUD, String.valueOf(repartidor.getLongitud()));
            }

            boolean success = editor.commit();
            Log.d("SessionManager", "Guardado completado: " + success);
        }
    }


    /**
     * Carga los datos del usuario desde Firestore
     */
    private void loadUserData(String userId, SessionCallback callback) {
        usuarioRepository.getUserById(userId)
                .addOnSuccessListener(usuario -> {
                    if (usuario == null) {
                        Log.e("SessionManager", "Usuario null desde repository");
                        callback.onSessionError();
                        return;
                    }

                    Log.d("SessionManager", "Tipo de usuario cargado: " + usuario.getTipoUsuario());
                    Log.d("SessionManager", "Clase del usuario: " + usuario.getClass().getSimpleName());

                    if (validateUserByType(usuario)) {
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
            Log.e("SessionManager", "Usuario o tipo de usuario null");
            return false;
        }

        if (!"Activo".equals(usuario.getEstado())) {
            Log.e("SessionManager", "Usuario no activo");
            return false;
        }

        try {
            switch (usuario.getTipoUsuario()) {
                case "AdministradorRestaurante":
//                    if (!(usuario instanceof AdministradorRestaurante)) {
//                        Log.e("SessionManager", "Usuario no es instancia de AdministradorRestaurante");
//                        return false;
//                    }
//                    AdministradorRestaurante admin = (AdministradorRestaurante) usuario;
//                    return admin.getRestauranteId() != null && !admin.getRestauranteId().isEmpty();
                    return usuario instanceof AdministradorRestaurante;
                case "Repartidor":
                    if (!(usuario instanceof Repartidor)) {
                        Log.e("SessionManager", "Usuario no es instancia de Repartidor");
                        return false;
                    }
                    Repartidor repartidor = (Repartidor) usuario;
                    return !"Ocupado".equals(repartidor.getEstado());

                case "Cliente":
                    return usuario instanceof Cliente;

                case "Superadmin":
                    return usuario instanceof Superadmin;

                default:
                    Log.e("SessionManager", "Tipo de usuario no válido: " + usuario.getTipoUsuario());
                    return false;
            }
        } catch (ClassCastException e) {
            Log.e("SessionManager", "Error al validar tipo de usuario", e);
            return false;
        }
    }

    /**
     * Cierra la sesión del usuario actual y limpia todos los datos locales
     * @return Task<Void> para manejar el resultado de la operación
     */
    public Task<Void> logout() {
        Log.d("SessionManager", "Iniciando proceso de cierre de sesión");

        // Limpiar datos en memoria
        usuarioActual = null;

        // Crear una tarea compuesta para el cierre de sesión
        return Tasks.call(() -> {
            // 1. Cerrar sesión en Firebase Auth
            auth.signOut();

            // 2. Limpiar SharedPreferences
            boolean prefsCleared = preferences.edit()
                    .clear()
                    .commit();

            if (!prefsCleared) {
                throw new Exception("Error al limpiar SharedPreferences");
            }

            // 3. Limpiar la instancia singleton
            instance = null;

            Log.d("SessionManager", "Sesión cerrada exitosamente");
            return null;
        });
    }

    /**
     * Sobrecarga del método logout para casos donde no se necesita el Task
     */
    public void logoutSync() {
        logout().addOnFailureListener(e ->
                Log.e("SessionManager", "Error en cierre de sesión", e)
        );
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
        Log.d("SessionManager", "isLoggedIn check - userId (Firestore): " + userId);
        Log.d("SessionManager", "isLoggedIn check - FirebaseUser: " +
                (currentUser != null ? "autenticado" : "no autenticado"));

        return isLoggedIn && currentUser != null && !userId.isEmpty();
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