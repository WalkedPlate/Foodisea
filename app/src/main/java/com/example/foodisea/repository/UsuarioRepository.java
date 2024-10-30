package com.example.foodisea.repository;

import android.util.Log;

import com.example.foodisea.model.AdministradorRestaurante;
import com.example.foodisea.model.Cliente;
import com.example.foodisea.model.Repartidor;
import com.example.foodisea.model.Superadmin;
import com.example.foodisea.model.Usuario;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Repositorio que maneja todas las operaciones relacionadas con usuarios en Firebase
 * Incluye operaciones de autenticación y gestión de datos en Firestore
 */
public class UsuarioRepository {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private static final String COLLECTION_USUARIOS = "usuarios";
    private static final String TAG = "UsuarioRepository";

    public UsuarioRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    /**
     * Realiza el inicio de sesión del usuario y obtiene sus datos completos
     * @param email Correo del usuario
     * @param password Contraseña del usuario
     * @return Task<Usuario> con los datos del usuario si el login es exitoso
     */
    public Task<Usuario> loginUser(String email, String password) {
        return auth.signInWithEmailAndPassword(email, password)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    return getUserDataByCorreo(email);
                });
    }


    /**
     * Obtiene los datos del usuario desde Firestore usando el campo correo
     * @param email Email del usuario
     * @return Task<Usuario> con los datos del usuario
     */
    private Task<Usuario> getUserDataByCorreo(String email) {
        return db.collection(COLLECTION_USUARIOS)
                .whereEqualTo("correo", email)
                .limit(1)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot.isEmpty()) {
                        throw new Exception("Usuario no encontrado en la base de datos");
                    }

                    DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                    String tipoUsuario = document.getString("tipoUsuario");

                    if (tipoUsuario == null) {
                        throw new Exception("Tipo de usuario no definido");
                    }

                    Usuario usuario = null;
                    String documentId = document.getId(); // Obtener ID del documento de Firestore

                    switch (tipoUsuario) {
                        case "AdministradorRestaurante":
                            AdministradorRestaurante admin = document.toObject(AdministradorRestaurante.class);
                            if (admin != null) {
                                admin.setId(documentId);
                            }
                            usuario = admin;
                            break;
                        case "Repartidor":
                            Repartidor repartidor = document.toObject(Repartidor.class);
                            if (repartidor != null) {
                                repartidor.setId(documentId);
                            }
                            usuario = repartidor;
                            break;
                        case "Cliente":
                            Cliente cliente = document.toObject(Cliente.class);
                            if (cliente != null) {
                                cliente.setId(documentId);
                            }
                            usuario = cliente;
                            break;
                        case "Superadmin":
                            Superadmin superadmin = document.toObject(Superadmin.class);
                            if (superadmin != null) {
                                superadmin.setId(documentId);
                            }
                            usuario = superadmin;
                            break;
                        default:
                            throw new Exception("Tipo de usuario no válido");
                    }

                    if (usuario == null) {
                        throw new Exception("Error al convertir datos del usuario");
                    }

                    return usuario;
                });
    }

    /**
     * Envía un correo para restablecer la contraseña
     * @param email Correo del usuario
     * @return Task<Void>
     */
    public Task<Void> sendPasswordResetEmail(String email) {
        return auth.sendPasswordResetEmail(email);
    }

    /**
     * Verifica si hay un usuario actualmente autenticado
     * @return Usuario actual o null si no hay sesión
     */
    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    /**
     * Cierra la sesión del usuario actual
     */
    public void logout() {
        auth.signOut();
    }

    /**
     * Actualiza el estado del usuario en Firestore
     * @param userId ID del usuario
     * @param nuevoEstado Nuevo estado a establecer
     * @return Task<Void>
     */
    public Task<Void> actualizarEstadoUsuario(String userId, String nuevoEstado) {
        return db.collection(COLLECTION_USUARIOS)
                .document(userId)
                .update("estado", nuevoEstado);
    }

    /**
     * Registra un nuevo usuario en Firebase Auth y Firestore
     * Soporta múltiples tipos de usuario (Cliente, Repartidor)
     * @param email Correo del usuario
     * @param password Contraseña del usuario
     * @param usuario Datos del usuario a registrar (Cliente o Repartidor)
     * @return Task<Usuario> con los datos del usuario registrado
     */
    public Task<Usuario> registerUser(String email, String password, Usuario usuario) {
        return auth.createUserWithEmailAndPassword(email, password)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    String uid = Objects.requireNonNull(task.getResult().getUser()).getUid();
                    return db.collection(COLLECTION_USUARIOS)
                            .document(uid)
                            .set(usuario)
                            .continueWith(firestoreTask -> usuario);
                });
    }

    /**
     * Obtiene el usuario por su ID
     * @return Task<Usuario>
     */
    public Task<Usuario> getUserById(String documentId) {
        return db.collection(COLLECTION_USUARIOS)
                .document(documentId)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful() || !task.getResult().exists()) {
                        throw new Exception("Usuario no encontrado en la base de datos");
                    }

                    DocumentSnapshot document = task.getResult();
                    String tipoUsuario = document.getString("tipoUsuario");

                    if (tipoUsuario == null) {
                        throw new Exception("Tipo de usuario no definido");
                    }

                    Usuario usuario = null;
                    switch (tipoUsuario) {
                        case "AdministradorRestaurante":
                            usuario = document.toObject(AdministradorRestaurante.class);
                            break;
                        case "Repartidor":
                            usuario = document.toObject(Repartidor.class);
                            break;
                        case "Cliente":
                            usuario = document.toObject(Cliente.class);
                            break;
                        case "Superadmin":
                            usuario = document.toObject(Superadmin.class);
                            break;
                        default:
                            throw new Exception("Tipo de usuario no válido");
                    }

                    if (usuario == null) {
                        throw new Exception("Error al convertir datos del usuario");
                    }

                    // Asegurar que el tipo de usuario esté establecido
                    usuario.setTipoUsuario(tipoUsuario);
                    usuario.setId(documentId);

                    return usuario;
                });
    }
}
