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
import java.util.concurrent.atomic.AtomicInteger;

public class ProductoRepository {
    private final FirebaseFirestore db;
    private final FirebaseStorage storage;
    private final String PRODUCTOS_COLLECTION = "productos";
    private final String PRODUCTOS_STORAGE = "productos";

    public ProductoRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
    }

    public interface UploadProgressListener {
        void onProgress(int imageIndex, int totalImages, double progress);
        void onImageUploaded(int completedUploads, int totalUploads, String imageUrl);
        void onError(String message);
    }

    public Task<Producto> crearProductoConImagenes(String nombre, String descripcion,
                                                   double precio, String categoria,
                                                   String restauranteId, List<Uri> imagenesUri,
                                                   UploadProgressListener progressListener) {
        // Validar número mínimo de imágenes
        if (imagenesUri.size() < 2) {
            return Tasks.forException(
                    new IllegalArgumentException("Se requieren mínimo 2 imágenes")
            );
        }

        // Generar ID único para el producto
        String productoId = UUID.randomUUID().toString();
        List<String> imageUrls = new ArrayList<>();
        AtomicInteger completedUploads = new AtomicInteger(0);
        int totalUploads = imagenesUri.size();

        // Referencia a la carpeta del producto en Storage
        StorageReference productoRef = storage.getReference()
                .child(PRODUCTOS_STORAGE)
                .child(productoId);

        List<Task<Uri>> uploadTasks = new ArrayList<>();

        // Iniciar subidas de imágenes
        for (int i = 0; i < imagenesUri.size(); i++) {
            Uri imageUri = imagenesUri.get(i);
            String fileName = "imagen_" + i + ".jpg";
            StorageReference imageRef = productoRef.child(fileName);

            UploadTask uploadTask = imageRef.putFile(imageUri);
            final int imageIndex = i;

            // Monitorear progreso de subida
            uploadTask.addOnProgressListener(snapshot -> {
                double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                if (progressListener != null) {
                    progressListener.onProgress(imageIndex + 1, totalUploads, progress);
                }
            });

            // Convertir UploadTask a Task<Uri>
            Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return imageRef.getDownloadUrl();
            }).addOnSuccessListener(uri -> {
                imageUrls.add(uri.toString());
                int completed = completedUploads.incrementAndGet();
                if (progressListener != null) {
                    progressListener.onImageUploaded(completed, totalUploads, uri.toString());
                }
            }).addOnFailureListener(e -> {
                if (progressListener != null) {
                    progressListener.onError("Error al subir imagen: " + e.getMessage());
                }
            });

            uploadTasks.add(urlTask);
        }

        // Esperar a que todas las imágenes se suban
        return Tasks.whenAllSuccess(uploadTasks)
                .continueWithTask(task -> {
                    // Crear objeto Producto
                    Producto producto = new Producto();
                    producto.setId(productoId);
                    producto.setNombre(nombre);
                    producto.setDescripcion(descripcion);
                    producto.setPrecio(precio);
                    producto.setCategoria(categoria);
                    producto.setRestauranteId(restauranteId);
                    producto.setImagenes(imageUrls);
                    producto.setOutOfStock(false);

                    // Guardar en Firestore
                    return db.collection(PRODUCTOS_COLLECTION)
                            .document(productoId)
                            .set(producto)
                            .continueWith(firestoreTask -> producto);
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

    public Task<List<Producto>> obtenerProductosPorRestaurante(String restauranteId) {
        return db.collection(PRODUCTOS_COLLECTION)
                .whereEqualTo("restauranteId", restauranteId)
                .get()
                .continueWith(task -> {
                    List<Producto> productos = new ArrayList<>();
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (DocumentSnapshot doc : task.getResult()) {
                            Producto producto = doc.toObject(Producto.class);
                            if (producto != null) {
                                productos.add(producto);
                            }
                        }
                    }
                    return productos;
                });
    }

    public Task<Producto> getProductoById(String productoId) {
        return db.collection(PRODUCTOS_COLLECTION)
                .document(productoId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        return task.getResult().toObject(Producto.class);
                    }
                    throw new Exception("Producto no encontrado");
                });
    }


}
