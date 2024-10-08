package com.example.foodisea.model;

import java.util.Date;
import java.util.Map;

public class Reporte {
    private String id;
    private String restauranteId;  // Referencia al Restaurante (ID)
    private Date fechaInicio;
    private Date fechaFin;
    private Map<String, Integer> ventasPorPlato;  // platoId -> Cantidad vendida
    private double totalVentas;


    //Constructor, getter y setter


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRestauranteId() {
        return restauranteId;
    }

    public void setRestauranteId(String restauranteId) {
        this.restauranteId = restauranteId;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Date getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(Date fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Map<String, Integer> getVentasPorPlato() {
        return ventasPorPlato;
    }

    public void setVentasPorPlato(Map<String, Integer> ventasPorPlato) {
        this.ventasPorPlato = ventasPorPlato;
    }

    public double getTotalVentas() {
        return totalVentas;
    }

    public void setTotalVentas(double totalVentas) {
        this.totalVentas = totalVentas;
    }
}

