package com.example.foodisea.model;

public class Usuario {
    protected String id;
    protected String nombres;
    protected String apellidos;
    protected String correo;
    protected String telefono;
    protected String direccion;
    protected String documentoId; //DNI
    protected String fechaNacimiento;
    protected String foto;
    protected String estado;  // "Activo", "Inactivo"
    protected String tipoUsuario;  // "Cliente", "AdministradorRestaurante", "Repartidor", "Superadmin"

    // Constructor con todos los parámetros
    public Usuario(String id, String nombre, String apellido, String email, String telefono, String direccion, String documentoId, String fechaNacimiento, String foto, String estado, String tipoUsuario) {
        this.id = id;
        this.nombres = nombre;
        this.apellidos = apellido;
        this.correo = email;
        this.telefono = telefono;
        this.direccion = direccion;
        this.documentoId = documentoId;
        this.fechaNacimiento = fechaNacimiento;
        this.foto = foto;
        this.estado = estado;
        this.tipoUsuario = tipoUsuario;
    }

    //Constructor, getter y setter

    public String getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(String tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getDocumentoId() {
        return documentoId;
    }

    public void setDocumentoId(String documentoId) {
        this.documentoId = documentoId;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
