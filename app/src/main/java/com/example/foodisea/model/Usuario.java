package com.example.foodisea.model;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Usuario {
    @Exclude
    protected String id;

    protected String nombres;
    protected String apellidos;
    protected String correo;
    protected String telefono;
    protected String direccion;
    protected Double latitudDireccion;
    protected Double longitudDireccion;
    protected String tipoDocumentoId;
    protected String documentoId; //DNI
    protected String fechaNacimiento;
    protected String foto;
    protected String estado;  // "Activo", "Inactivo"
    protected String tipoUsuario;  // "Cliente", "AdministradorRestaurante", "Repartidor", "Superadmin"

    public Usuario() {
    }

    // Constructor con todos los parámetros
    public Usuario(String id, String nombre, String apellido, String email, String telefono, String direccion, String tipoDocumentoId, String documentoId, String fechaNacimiento, String foto, String estado, String tipoUsuario) {
        this.id = id;
        this.nombres = nombre;
        this.apellidos = apellido;
        this.correo = email;
        this.telefono = telefono;
        this.direccion = direccion;
        this.tipoDocumentoId = tipoDocumentoId;
        this.documentoId = documentoId;
        this.fechaNacimiento = fechaNacimiento;
        this.foto = foto;
        this.estado = estado;
        this.tipoUsuario = tipoUsuario;
    }

    public String obtenerNombreCompleto() {
        StringBuilder nombreCompleto = new StringBuilder();

        if (nombres != null && !nombres.isEmpty()) {
            nombreCompleto.append(nombres);
        }

        if (apellidos != null && !apellidos.isEmpty()) {
            if (nombreCompleto.length() > 0) {
                nombreCompleto.append(" ");
            }
            nombreCompleto.append(apellidos);
        }

        return nombreCompleto.length() > 0 ? nombreCompleto.toString() : "Sin nombre";
    }

    //Constructor, getter y setter

    public String getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(String tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
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

    public String getTipoDocumentoId() {
        return tipoDocumentoId;
    }

    public void setTipoDocumentoId(String tipoDocumentoId) {
        this.tipoDocumentoId= tipoDocumentoId;
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

    public Double getLongitudDireccion() {
        return longitudDireccion;
    }

    public void setLongitudDireccion(Double longitudDireccion) {
        this.longitudDireccion = longitudDireccion;
    }

    public Double getLatitudDireccion() {
        return latitudDireccion;
    }

    public void setLatitudDireccion(Double latitudDireccion) {
        this.latitudDireccion = latitudDireccion;
    }
}
