package com.example.foodisea.model;

public class Cliente extends Usuario {

    // Constructor de la clase Cliente
    public Cliente(String id, String nombres, String apellidos, String correo, String telefono, String direccion,
                   String documentoId, String fechaNacimiento, String foto, String estado, String tipoUsuario) {
        this.id = id;  // Heredado de Usuario
        this.nombres = nombres;  // Heredado de Usuario
        this.apellidos = apellidos;  // Heredado de Usuario
        this.correo = correo;  // Heredado de Usuario
        this.telefono = telefono;  // Heredado de Usuario
        this.direccion = direccion;  // Heredado de Usuario
        this.documentoId = documentoId;  // Heredado de Usuario
        this.fechaNacimiento = fechaNacimiento;  // Heredado de Usuario
        this.foto = foto;  // Heredado de Usuario
        this.estado = estado;  // Heredado de Usuario
        this.tipoUsuario = tipoUsuario; // Heredado de Usuario
    }

    // Métodos específicos para Cliente.
    public void registrarPedido(Pedido pedido) {
        // Lógica para registrar un pedido
    }

    //Constructor, getter y setter


}

