package com.example.foodisea.model;

import java.io.Serializable;

public class ProductoCantidad implements Serializable {
    private String productoId;  // ID del producto
    private int cantidad;     // Cantidad del producto solicitada

    public ProductoCantidad(){

    }

    // Constructor
    public ProductoCantidad(String productoId, int cantidad) {
        this.productoId = productoId;
        this.cantidad = cantidad;
    }

    // Getters y Setters

    public String getProductoId() {
        return productoId;
    }

    public void setProductoId(String productoId) {
        this.productoId = productoId;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
