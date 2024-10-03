package com.example.foodisea.model;

public class AdministradorRestaurante extends Usuario {
    private String restauranteId;


    // Constructor que llama a la clase Usuario
    public AdministradorRestaurante(String id, String nombres, String apellidos, String correo, String telefono,
                                    String direccion, String documentoId, String fechaNacimiento, String foto,
                                    String estado, String tipoUsuario, String restauranteId) {
        super(id, nombres, apellidos, correo, telefono, direccion, documentoId, fechaNacimiento, foto, estado, tipoUsuario);
        this.restauranteId = restauranteId;
    }

    public void registrarPlato(Plato plato) {
        // Lógica para registrar un plato
    }

    public void verReporteVentas() {
        // Lógica para ver el reporte de ventas
    }



    //Constructor, getter y setter

    public String getRestauranteId() {
        return restauranteId;
    }

    public void setRestauranteId(String restauranteId) {
        this.restauranteId = restauranteId;
    }


}

