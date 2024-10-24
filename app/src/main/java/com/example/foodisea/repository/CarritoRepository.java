package com.example.foodisea.repository;

import com.example.foodisea.model.Carrito;
import com.example.foodisea.model.ProductoCantidad;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CarritoRepository {
    private final FirebaseFirestore db;

    public CarritoRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    // Crear o actualizar carrito
    public Task<Void> guardarCarrito(Carrito carrito) {
        return db.collection("carritos")
                .document(carrito.getId())
                .set(carrito);
    }

    // Obtener carrito activo del cliente
    public Task<Carrito> getCarritoActivo(String clienteId) {
        return db.collection("carritos")
                .whereEqualTo("clienteId", clienteId)
                .limit(1)
                .get()
                .continueWith(task -> {
                    if (task.getResult().isEmpty()) {
                        return null;
                    }
                    return task.getResult().getDocuments().get(0).toObject(Carrito.class);
                });
    }

    // Agregar producto al carrito
    public Task<Void> agregarProducto(String carritoId, ProductoCantidad producto) {
        return db.collection("carritos")
                .document(carritoId)
                .get()
                .continueWithTask(task -> {
                    Carrito carrito = task.getResult().toObject(Carrito.class);
                    List<ProductoCantidad> productos = carrito.getProductos();
                    productos.add(producto);
                    return db.collection("carritos")
                            .document(carritoId)
                            .update("productos", productos, "total", carrito.calcularTotal());
                });
    }

    // Eliminar producto del carrito
    public Task<Void> eliminarProducto(String carritoId, String productoId) {
        return db.collection("carritos")
                .document(carritoId)
                .get()
                .continueWithTask(task -> {
                    Carrito carrito = task.getResult().toObject(Carrito.class);
                    List<ProductoCantidad> productos = carrito.getProductos();
                    productos.removeIf(p -> p.getProductoId().equals(productoId));
                    return db.collection("carritos")
                            .document(carritoId)
                            .update("productos", productos, "total", carrito.calcularTotal());
                });
    }

    // Limpiar carrito después de crear pedido
    public Task<Void> limpiarCarrito(String carritoId) {
        return db.collection("carritos")
                .document(carritoId)
                .delete();
    }
}
