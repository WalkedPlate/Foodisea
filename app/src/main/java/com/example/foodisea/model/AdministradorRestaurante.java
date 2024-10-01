package com.example.foodisea.model;

public class AdministradorRestaurante extends Usuario {
    private String restauranteId;

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

