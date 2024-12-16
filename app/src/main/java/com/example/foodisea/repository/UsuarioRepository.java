package com.example.foodisea.repository;

import android.net.Uri;
import android.util.Log;

import com.example.foodisea.manager.LogManager;
import com.example.foodisea.model.AdministradorRestaurante;
import com.example.foodisea.model.Cliente;
import com.example.foodisea.model.Producto;
import com.example.foodisea.model.Repartidor;
import com.example.foodisea.model.Superadmin;
import com.example.foodisea.model.Usuario;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

/**
 * Repositorio que maneja todas las operaciones relacionadas con usuarios en Firebase
 * Incluye operaciones de autenticación y gestión de datos en Firestore
 */
public class UsuarioRepository {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final FirebaseStorage storage;
    private static final String COLLECTION_USUARIOS = "usuarios";
    private static final String STORAGE_USUARIOS = "usuarios";
    private static final String TAG = "UsuarioRepository";


    public UsuarioRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.storage = FirebaseStorage.getInstance();
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
     * Registra un nuevo usuario en Firebase Auth y Firestore, incluyendo su foto de perfil
     * @param email Correo del usuario
     * @param password Contraseña del usuario
     * @param usuario Datos del usuario a registrar
     * @param photoUri URI de la foto de perfil (puede ser null)
     * @return Task<Usuario> con los datos del usuario registrado
     */
    public Task<Usuario> registerUser(String email, String password, Usuario usuario, @Nullable Uri photoUri) {
        LogManager logManager = new LogManager();
        return auth.createUserWithEmailAndPassword(email, password)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }

                    String uid = Objects.requireNonNull(task.getResult().getUser()).getUid();

                    // Si no hay foto, registrar usuario directamente
                    if (photoUri == null) {
                        usuario.setFoto("");
                        return db.collection(COLLECTION_USUARIOS)
                                .document(uid)
                                .set(usuario)
                                .continueWith(firestoreTask -> {
                                    // Registrar log de éxito
                                    logManager.createLog(
                                            uid,
                                            "REGISTER_USER",
                                            "Usuario registrado exitosamente con email: " + email
                                    );
                                    return usuario;
                                });
                    }

                    // Si hay foto, subirla primero y luego registrar usuario
                    return uploadUserPhoto(photoUri, uid)
                            .continueWithTask(uploadTask -> {
                                String photoUrl = uploadTask.getResult();
                                usuario.setFoto(photoUrl);
                                return db.collection(COLLECTION_USUARIOS)
                                        .document(uid)
                                        .set(usuario)
                                        .continueWith(firestoreTask -> {
                                            // Registrar log de éxito
                                            logManager.createLog(
                                                    uid,
                                                    "REGISTER_USER",
                                                    "Usuario registrado exitosamente email: " + email
                                            );
                                            return usuario;
                                        });
                            });
                });
    }

    /**
     * Sube una foto de usuario a Firebase Storage en la carpeta correspondiente
     * @param imageUri URI de la imagen a subir
     * @param userId ID del usuario
     * @return Task<String> con la URL de descarga de la imagen
     */
    private Task<String> uploadUserPhoto(Uri imageUri, String userId) {
        StorageReference photoRef = storage.getReference()
                .child(STORAGE_USUARIOS)
                .child(userId)
                .child("profile.jpg");

        return photoRef.putFile(imageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return photoRef.getDownloadUrl();
                })
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return task.getResult().toString();
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

    public Task<List<Usuario>> getUsuariosPorTipo(String tipoUsuario){
        return db.collection(COLLECTION_USUARIOS)
                .whereEqualTo("tipoUsuario",tipoUsuario)
                .get()
                .continueWith(task -> {
                   List<Usuario> usuarios = new ArrayList<>();
                   if(task.isSuccessful() && task.getResult() != null){
                       for (DocumentSnapshot doc : task.getResult()) {
                            Usuario usuario = doc.toObject(Usuario.class);
                           if (usuario != null) {
                               usuario.setId(doc.getId()); // Establecer ID del documento
                               usuarios.add(usuario);
                           }
                       }
                   }
                   return usuarios;
                });
    }

    public Task<List<Usuario>> getUsuariosPorTipoYEstados(String tipoUsuario, List<String> estadosPermitidos) {
        return db.collection(COLLECTION_USUARIOS)
                .whereEqualTo("tipoUsuario", tipoUsuario) // Filtrar por tipoUsuario
                .get()
                .continueWith(task -> {
                    List<Usuario> usuarios = new ArrayList<>();
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (DocumentSnapshot doc : task.getResult()) {
                            Usuario usuario = doc.toObject(Usuario.class);
                            // Verificar si el estado está en la lista de estados permitidos
                            if (usuario != null && estadosPermitidos.contains(usuario.getEstado())) {
                                usuario.setId(doc.getId()); // Establecer ID del documento
                                usuarios.add(usuario);
                            }
                        }
                    }
                    return usuarios;
                });
    }

    public Task<List<AdministradorRestaurante>> getAdministradoresRestaurantes(){
        return db.collection(COLLECTION_USUARIOS)
                .whereEqualTo("tipoUsuario","AdministradorRestaurante")
                .get()
                .continueWith(task -> {
                    List<AdministradorRestaurante> listaAdministradores = new ArrayList<>();
                    if(task.isSuccessful() && task.getResult() != null){
                        for (DocumentSnapshot doc : task.getResult()) {
                            AdministradorRestaurante administradorRestaurante = doc.toObject(AdministradorRestaurante.class);
                            if (administradorRestaurante != null) {
                                administradorRestaurante.setId(doc.getId()); // Establecer ID del documento
                                listaAdministradores.add(administradorRestaurante);
                            }
                        }
                    }
                    return listaAdministradores;
                });
    }

    public Task<List<AdministradorRestaurante>> getAdministradoresLibres(){
        return db.collection(COLLECTION_USUARIOS)
                .whereEqualTo("tipoUsuario","AdministradorRestaurante")
                .whereEqualTo("restauranteId",null)
                .get()
                .continueWith(task -> {
                    List<AdministradorRestaurante> listaAdministradores = new ArrayList<>();
                    if(task.isSuccessful() && task.getResult() != null){
                        for (DocumentSnapshot doc : task.getResult()) {
                            AdministradorRestaurante administradorRestaurante = doc.toObject(AdministradorRestaurante.class);
                            if (administradorRestaurante != null) {
                                administradorRestaurante.setId(doc.getId()); // Establecer ID del documento
                                listaAdministradores.add(administradorRestaurante);
                            }
                        }
                    }
                    return listaAdministradores;
                });
    }

    public Task<Void> actualizarEstadoUsuario(Usuario usuario, String nuevoEstado) {
        return FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(usuario.getId())  // Usa el ID correcto del usuario
                .update("estado", nuevoEstado);
    }

    public Task<Void> asignarRestauranteAAdministrador(String adminId, String restauranteId) {
        return db.collection(COLLECTION_USUARIOS)
                .document(adminId)
                .update("restauranteId", restauranteId);
    }


}
