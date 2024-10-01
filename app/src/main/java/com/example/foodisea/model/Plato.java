package com.example.foodisea.model;

import java.util.List;

public class Plato {
    private String id;
    private String nombre;
    private String descripcion;
    private double precio;
    private List<String> imagenes;  // Mínimo 2 imágenes
    private String categoria; // "Plato" o "Bebida"
    private boolean outOfStock;

    // Constructor, getters y setters
    public Plato(String id, String nombre, String descripcion, double precio, List<String> imagenes, String categoria, boolean outOfStock) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.imagenes = imagenes;
        this.categoria = categoria;
        this.outOfStock = outOfStock;
    }


    public boolean isOutOfStock() {
        return outOfStock;
    }

    public void setOutOfStock(boolean outOfStock) {
        this.outOfStock = outOfStock;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public List<String> getImagenes() {
        return imagenes;
    }

    public void setImagenes(List<String> imagenes) {
        this.imagenes = imagenes;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
}
