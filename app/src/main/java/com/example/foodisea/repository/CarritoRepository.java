package com.example.foodisea.repository;

import com.example.foodisea.model.Carrito;
import com.example.foodisea.model.Producto;
import com.example.foodisea.model.ProductoCantidad;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CarritoRepository {
    private final FirebaseFirestore db;
    private final String CARRITOS_COLLECTION = "carritos";

    public CarritoRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    // Obtener el carrito activo del cliente
    public Task<DocumentSnapshot> obtenerCarritoActivo(String clienteId) {
        return db.collection(CARRITOS_COLLECTION)
                .document(clienteId)
                .get();
    }

    // Agregar o actualizar producto en el carrito
    public Task<Void> agregarProductoAlCarrito(String clienteId, String restauranteId,
                                               Producto producto, int cantidad) {
        return obtenerCarritoActivo(clienteId).continueWithTask(task -> {
            DocumentSnapshot document = task.getResult();

            if (document.exists()) {
                Carrito carrito = document.toObject(Carrito.class);

                // Verificar si el producto es del mismo restaurante
                if (!carrito.getRestauranteId().equals(restauranteId)) {
                    return Tasks.forException(
                            new Exception("No puedes agregar productos de diferentes restaurantes al carrito")
                    );
                }

                // Actualizar producto existente o agregar nuevo
                List<ProductoCantidad> productos = carrito.getProductos();
                boolean productoEncontrado = false;

                for (ProductoCantidad pc : productos) {
                    if (pc.getProductoId().equals(producto.getId())) {
                        pc.setCantidad(pc.getCantidad() + cantidad);
                        productoEncontrado = true;
                        break;
                    }
                }

                if (!productoEncontrado) {
                    ProductoCantidad nuevoPc = new ProductoCantidad();
                    nuevoPc.setProductoId(producto.getId());
                    nuevoPc.setCantidad(cantidad);
                    productos.add(nuevoPc);
                }

                carrito.setTotal(carrito.getTotal() + (producto.getPrecio() * cantidad));

                return db.collection(CARRITOS_COLLECTION)
                        .document(clienteId)
                        .set(carrito);

            } else {
                // Crear nuevo carrito
                Carrito nuevoCarrito = new Carrito();
                nuevoCarrito.setClienteId(clienteId);
                nuevoCarrito.setRestauranteId(restauranteId);

                List<ProductoCantidad> productos = new ArrayList<>();
                ProductoCantidad pc = new ProductoCantidad();
                pc.setProductoId(producto.getId());
                pc.setCantidad(cantidad);
                productos.add(pc);

                nuevoCarrito.setProductos(productos);
                nuevoCarrito.setTotal(producto.getPrecio() * cantidad);

                return db.collection(CARRITOS_COLLECTION)
                        .document(clienteId)
                        .set(nuevoCarrito);
            }
        });
    }

    // Actualizar la cantidad de un producto en el carrito
    public Task<Void> actualizarCantidadProducto(String clienteId, String productoId,
                                                 int nuevaCantidad, double precioProducto) {
        return obtenerCarritoActivo(clienteId).continueWithTask(task -> {
            DocumentSnapshot document = task.getResult();
            if (!document.exists()) {
                return Tasks.forException(new Exception("Carrito no encontrado"));
            }

            Carrito carrito = document.toObject(Carrito.class);
            List<ProductoCantidad> productos = carrito.getProductos();

            // Encontrar y actualizar el producto
            for (ProductoCantidad pc : productos) {
                if (pc.getProductoId().equals(productoId)) {
                    // Calcular la diferencia en el total
                    int diferenciaCantidad = nuevaCantidad - pc.getCantidad();
                    double diferenciaPrecio = diferenciaCantidad * precioProducto;

                    // Actualizar cantidad y total
                    pc.setCantidad(nuevaCantidad);
                    carrito.setTotal(carrito.getTotal() + diferenciaPrecio);
                    break;
                }
            }

            // Si la cantidad es 0, eliminar el producto
            productos.removeIf(pc -> pc.getCantidad() <= 0);

            // Si no quedan productos, eliminar el carrito
            if (productos.isEmpty()) {
                return db.collection(CARRITOS_COLLECTION)
                        .document(clienteId)
                        .delete();
            }

            return db.collection(CARRITOS_COLLECTION)
                    .document(clienteId)
                    .set(carrito);
        });
    }

    // Eliminar un producto del carrito
    public Task<Void> eliminarProductoDelCarrito(String clienteId, String productoId) {
        return obtenerCarritoActivo(clienteId).continueWithTask(task -> {
            DocumentSnapshot document = task.getResult();
            if (!document.exists()) {
                return Tasks.forException(new Exception("Carrito no encontrado"));
            }

            Carrito carrito = document.toObject(Carrito.class);
            List<ProductoCantidad> productos = carrito.getProductos();

            // Encontrar y eliminar el producto
            ProductoCantidad productoAEliminar = null;
            for (ProductoCantidad pc : productos) {
                if (pc.getProductoId().equals(productoId)) {
                    productoAEliminar = pc;
                    break;
                }
            }

            if (productoAEliminar != null) {
                productos.remove(productoAEliminar);

                // Si no quedan productos, eliminar el carrito
                if (productos.isEmpty()) {
                    return db.collection(CARRITOS_COLLECTION)
                            .document(clienteId)
                            .delete();
                }

                // Recalcular el total
                return recalcularTotal(clienteId);
            }

            return Tasks.forException(new Exception("Producto no encontrado en el carrito"));
        });
    }

    // Método auxiliar para recalcular el total del carrito
    private Task<Void> recalcularTotal(String clienteId) {
        return obtenerCarritoActivo(clienteId).continueWithTask(task -> {
            DocumentSnapshot document = task.getResult();
            Carrito carrito = document.toObject(Carrito.class);

            if (carrito == null || carrito.getProductos() == null || carrito.getProductos().isEmpty()) {
                return Tasks.forResult(null);
            }

            // Lista para almacenar todas las tareas de obtención de productos
            List<Task<DocumentSnapshot>> productoTasks = new ArrayList<>();

            // Crear tareas para obtener cada producto
            for (ProductoCantidad pc : carrito.getProductos()) {
                Task<DocumentSnapshot> productoTask = db.collection("productos")
                        .document(pc.getProductoId())
                        .get();
                productoTasks.add(productoTask);
            }

            // Esperar a que se completen todas las tareas
            return Tasks.whenAllComplete(productoTasks).continueWithTask(allTask -> {
                double nuevoTotal = 0;

                // Iterar sobre las tareas completadas
                for (int i = 0; i < allTask.getResult().size(); i++) {
                    Task<DocumentSnapshot> productoTask = productoTasks.get(i);
                    if (productoTask.isSuccessful() && productoTask.getResult() != null) {
                        DocumentSnapshot doc = productoTask.getResult();
                        Producto producto = doc.toObject(Producto.class);
                        if (producto != null) {
                            ProductoCantidad pc = carrito.getProductos().get(i);
                            nuevoTotal += producto.getPrecio() * pc.getCantidad();
                        }
                    }
                }

                carrito.setTotal(nuevoTotal);

                return db.collection(CARRITOS_COLLECTION)
                        .document(clienteId)
                        .set(carrito);
            });
        });
    }

    // Limpiar el carrito completamente
    public Task<Void> limpiarCarrito(String clienteId) {
        return db.collection(CARRITOS_COLLECTION)
                .document(clienteId)
                .delete();
    }
}
