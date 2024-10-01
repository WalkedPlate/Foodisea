package com.example.foodisea.model;

public class PlatoCantidad {
    private String platoId;  // ID del plato
    private int cantidad;     // Cantidad del plato solicitada

    // Constructor
    public PlatoCantidad(String platoId, int cantidad) {
        this.platoId = platoId;
        this.cantidad = cantidad;
    }

    // Getters y Setters
    public String getPlatoId() {
        return platoId;
    }

    public void setPlatoId(String platoId) {
        this.platoId = platoId;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
