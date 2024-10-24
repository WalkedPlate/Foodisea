package com.example.foodisea.repository;

import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.example.foodisea.model.Producto;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProductoRepository {
    private final FirebaseFirestore db;
    private final FirebaseStorage storage;
    private final String PRODUCTOS_COLLECTION = "productos";
    private final String PRODUCTOS_STORAGE = "productos";

    public ProductoRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
    }

    public Task<Producto> crearProductoConImagenes(String nombre, String descripcion,
                                                   double precio, String categoria,
                                                   String restauranteId, List<Uri> imagenesUri) {
        // Validar número mínimo de imágenes
        if (imagenesUri.size() < 2) {
            return Tasks.forException(
                    new IllegalArgumentException("Se requieren mínimo 2 imágenes")
            );
        }

        // Generar ID único para el producto
        String productoId = UUID.randomUUID().toString();

        // Crear lista de tareas de subida de imágenes
        List<UploadTask> uploadTasks = new ArrayList<>();
        List<String> imageUrls = new ArrayList<>();

        // Referencia a la carpeta del producto en Storage
        StorageReference productoRef = storage.getReference()
                .child(PRODUCTOS_STORAGE)
                .child(productoId);

        // Preparar todas las subidas de imágenes
        for (int i = 0; i < imagenesUri.size(); i++) {
            Uri imageUri = imagenesUri.get(i);
            String extension = getFileExtension(imageUri);
            String fileName = "imagen_" + i + "." + extension;
            StorageReference imageRef = productoRef.child(fileName);

            uploadTasks.add(imageRef.putFile(imageUri));
        }

        // Ejecutar todas las subidas y obtener URLs
        return Tasks.whenAllSuccess(uploadTasks)
                .continueWithTask(uploadResults -> {
                    List<Task<Uri>> downloadUrlTasks = new ArrayList<>();

                    List<Object> results = uploadResults.getResult();
                    for (Object result : results) {
                        UploadTask.TaskSnapshot snapshot = (UploadTask.TaskSnapshot) result;
                        downloadUrlTasks.add(snapshot.getStorage().getDownloadUrl());
                    }

                    return Tasks.whenAllSuccess(downloadUrlTasks);
                })
                .continueWithTask(urlResults -> {
                    // Recolectar todas las URLs
                    List<Object> results = urlResults.getResult();
                    for (Object urlResult : results) {
                        imageUrls.add(((Uri) urlResult).toString());
                    }

                    // Crear objeto Producto
                    Producto producto = new Producto(
                            productoId,
                            nombre,
                            descripcion,
                            precio,
                            imageUrls,
                            categoria,
                            false // outOfStock inicial
                    );

                    // Guardar en Firestore
                    return db.collection(PRODUCTOS_COLLECTION)
                            .document(productoId)
                            .set(producto)
                            .continueWith(task -> producto);
                });
    }

    // Método auxiliar para obtener la extensión del archivo
    private String getFileExtension(Uri uri) {
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(uri.toString())
        );
    }

    // Método para eliminar un producto y sus imágenes
    public Task<Void> eliminarProducto(String productoId) {
        // Primero eliminar todas las imágenes en Storage
        StorageReference productoRef = storage.getReference()
                .child(PRODUCTOS_STORAGE)
                .child(productoId);

        return productoRef.listAll()
                .continueWithTask(listResult -> {
                    List<Task<Void>> deleteTasks = new ArrayList<>();

                    // Agregar tarea de eliminación para cada imagen
                    for (StorageReference item : listResult.getResult().getItems()) {
                        deleteTasks.add(item.delete());
                    }

                    // Agregar tarea de eliminación del documento en Firestore
                    deleteTasks.add(
                            db.collection(PRODUCTOS_COLLECTION)
                                    .document(productoId)
                                    .delete()
                    );

                    return Tasks.whenAll(deleteTasks);
                });
    }
}
