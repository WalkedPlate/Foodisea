package com.example.foodisea.model;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.List;

@IgnoreExtraProperties
public class Carrito {
    @Exclude
    private String id;
    private String clienteId;  // Referencia al cliente (ID)
    private String restauranteId;  // ID del restaurante
    private List<ProductoCantidad> productos;  // Lista de productos y cantidades
    private double total;

    // Constructor
    public Carrito(String id, String clienteId, String restauranteId, List<ProductoCantidad> productos) {
        this.id = id;
        this.clienteId = clienteId;
        this.restauranteId = restauranteId;
        this.productos = productos;
        this.total = calcularTotal();  // Inicializa el total
    }

    public Carrito() {
    }

    // Método para calcular el total del carrito
    public double calcularTotal() {
        double total = 0.0;
        for (ProductoCantidad productoCantidad : productos) {
            // Suponiendo que hay un método para obtener el precio de un producto por su ID
            double precioproducto = obtenerPrecioPorId(productoCantidad.getProductoId());
            total += precioproducto * productoCantidad.getCantidad();
        }
        return total;
    }

    // Método para obtener el precio de un producto dado su ID (puedes modificarlo según tu lógica)
    private double obtenerPrecioPorId(String productoId) {
        // Aquí iría la lógica para obtener el precio del producto.
        // Por ahora, devolveremos un valor de ejemplo.
        // Debes implementarlo según tu fuente de datos (base de datos, etc.).
        return 10.0;  // Reemplaza este valor con el precio real del producto.
    }


    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

    public String getRestauranteId() {
        return restauranteId;
    }

    public void setRestauranteId(String restauranteId) {
        this.restauranteId = restauranteId;
    }

    public List<ProductoCantidad> getProductos() {
        return productos;
    }

    public void setProductos(List<ProductoCantidad> productos) {
        this.productos = productos;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}