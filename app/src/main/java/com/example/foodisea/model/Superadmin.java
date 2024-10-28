package com.example.foodisea.model;

public class Superadmin extends Usuario {
    // Constructor que llama a la clase Usuario
    public Superadmin(String id, String nombres, String apellidos, String correo, String telefono, String direccion,
                      String documentoId, String fechaNacimiento, String foto, String estado, String tipoUsuario) {
        super(id, nombres, apellidos, correo, telefono, direccion, documentoId, fechaNacimiento, foto, estado, tipoUsuario);
    }

    public Superadmin() {
    }

    public void habilitarUsuario(Usuario usuario) {
        // Lógica para habilitar un usuario
    }

    public void generarReporteGeneral() {
        // Lógica para generar reportes generales del sistema
    }
}
