package com.example.foodisea.model;

import java.util.ArrayList;
import java.util.List;

public class Restaurante {
    private String id;
    private String nombre;
    private String direccion;
    private String telefono;
    private List<String> platosIds;  // Lista de IDs de los platos o bebidas
    private String estado;  // "Activo" o "Inactivo"
    private List<String> categorias;  // Lista de categorías (e.g., "Burger", "Chicken", etc.)
    private double rating;  // Calificación del restaurante
    private List<String> imagenes;  // Lista de URLs o recursos de imágenes
    private AdministradorRestaurante administrador;  // Referencia al administrador

    //Constructor, getter y setter

    // Constructor con parámetros
    public Restaurante(String nombre, String direccion, String telefono, List<String> categorias, double rating, List<String> imagenes, AdministradorRestaurante administrador) {
        this.id = generateId(); // Asigna un ID único, si es necesario
        this.nombre = nombre;
        this.direccion = direccion;
        this.telefono = telefono;
        this.categorias = categorias;
        this.rating = rating;
        this.imagenes = imagenes; // Lista de nombres de recursos de imágenes
        this.administrador = administrador;
        this.estado = "Activo"; // Estado predeterminado
    }

    // Método para generar un ID único (por ahora)
    private String generateId() {
        return String.valueOf(System.currentTimeMillis()); // Ejemplo simple de generación de ID
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

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public List<String> getPlatosIds() {
        return platosIds;
    }

    public void setPlatosIds(List<String> platosIds) {
        this.platosIds = platosIds;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public List<String> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<String> categorias) {
        this.categorias = categorias;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public List<String> getImagenes() {
        return imagenes;
    }

    public void setImagenes(List<String> imagenes) {
        this.imagenes = imagenes;
    }

    public AdministradorRestaurante getAdministrador() {
        return administrador;
    }

    public void setAdministrador(AdministradorRestaurante administrador) {
        this.administrador = administrador;
    }
}
