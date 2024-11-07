package com.example.foodisea.model;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.List;

@IgnoreExtraProperties
public class Restaurante {
    @Exclude
    private String id;

    private String nombre;
    private String direccion;
    private String telefono;
    private String estado;  // "Activo" o "Inactivo"
    private List<String> categorias;  // Lista de categorías (e.g., "Burger", "Chicken", etc.)
    private double rating;  // Calificación del restaurante
    private List<String> imagenes;  // Lista de URLs o recursos de imágenes
    private String administradorId;  // Referencia al administrador (ID)
    private String descripcion;

    //Constructor
    public Restaurante() {
    }

    // Constructor con parámetros
    public Restaurante(String nombre, String direccion, String telefono, List<String> categorias, double rating, List<String> imagenes, String administradorId, String descripcion) {
        this.id = generateId(); // Asigna un ID único, si es necesario
        this.nombre = nombre;
        this.direccion = direccion;
        this.telefono = telefono;
        this.categorias = categorias;
        this.rating = rating;
        this.imagenes = imagenes; // Lista de nombres de recursos de imágenes
        this.administradorId = administradorId;
        this.estado = "Activo"; // Estado predeterminado
        this.descripcion = descripcion;
    }

    // Método para generar un ID único (por ahora)
    private String generateId() {
        return String.valueOf(System.currentTimeMillis()); // Ejemplo simple de generación de ID
    }



    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
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

    public String getAdministradorId() {
        return administradorId;
    }

    public void setAdministradorId(String administradorId) {
        this.administradorId = administradorId;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion(){return descripcion;}
}
