package com.example.foodisea.model;

import java.util.List;

public class Carrito {
    private String id;
    private String clienteId;  // Referencia al cliente (ID)
    private String restauranteId;  // ID del restaurante
    private List<PlatoCantidad> platos;  // Lista de platos y cantidades
    private double total;

    // Constructor
    public Carrito(String id, String clienteId, String restauranteId, List<PlatoCantidad> platos) {
        this.id = id;
        this.clienteId = clienteId;
        this.restauranteId = restauranteId;
        this.platos = platos;
        this.total = calcularTotal();  // Inicializa el total
    }

    // Método para calcular el total del carrito
    private double calcularTotal() {
        double total = 0.0;
        for (PlatoCantidad platoCantidad : platos) {
            // Suponiendo que hay un método para obtener el precio de un plato por su ID
            double precioPlato = obtenerPrecioPorId(platoCantidad.getPlatoId());
            total += precioPlato * platoCantidad.getCantidad();
        }
        return total;
    }

    // Método para obtener el precio de un plato dado su ID (puedes modificarlo según tu lógica)
    private double obtenerPrecioPorId(String platoId) {
        // Aquí iría la lógica para obtener el precio del plato.
        // Por ahora, devolveremos un valor de ejemplo.
        // Debes implementarlo según tu fuente de datos (base de datos, etc.).
        return 10.0;  // Reemplaza este valor con el precio real del plato.
    }


    public String getId() {
        return id;
    }

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

    public List<PlatoCantidad> getPlatos() {
        return platos;
    }

    public void setPlatos(List<PlatoCantidad> platos) {
        this.platos = platos;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}