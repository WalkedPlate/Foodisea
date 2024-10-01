package com.example.foodisea.models;

import java.util.List;

public class Restaurante {
    private String id;
    private String nombre;
    private String direccion;
    private String telefono;
    private List<String> platosIds;  // Lista de IDs de los platos o bebidas
    private String estado;  // "Activo" o "Inactivo"
    private AdministradorRestaurante administrador;  // Referencia al administrador

//Constructor, getter y setter

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

    public AdministradorRestaurante getAdministrador() {
        return administrador;
    }

    public void setAdministrador(AdministradorRestaurante administrador) {
        this.administrador = administrador;
    }
}
