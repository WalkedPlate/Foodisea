package com.example.foodisea.model;

import java.util.Date;
import java.util.Map;

public class Reporte {
    private String id;
    private Restaurante restaurante;  // Referencia al Restaurante del cual se generan las ventas
    private Date fechaInicio;
    private Date fechaFin;
    private Map<Plato, Integer> ventasPorPlato;  // Plato -> Cantidad vendida
    private double totalVentas;


    //Constructor, getter y setter

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Restaurante getRestaurante() {
        return restaurante;
    }

    public void setRestaurante(Restaurante restaurante) {
        this.restaurante = restaurante;
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

    public Map<Plato, Integer> getVentasPorPlato() {
        return ventasPorPlato;
    }

    public void setVentasPorPlato(Map<Plato, Integer> ventasPorPlato) {
        this.ventasPorPlato = ventasPorPlato;
    }

    public double getTotalVentas() {
        return totalVentas;
    }

    public void setTotalVentas(double totalVentas) {
        this.totalVentas = totalVentas;
    }
}

