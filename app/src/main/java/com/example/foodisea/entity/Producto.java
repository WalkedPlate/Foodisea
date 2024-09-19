package com.example.foodisea.entity;

public class Producto {
    private String name;
    private double price;
    private int imageResource;
    private boolean outOfStock;

    public Producto(String name, double price, int imageResource, boolean outOfStock) {
        this.name = name;
        this.price = price;
        this.imageResource = imageResource;
        this.outOfStock = outOfStock;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getImageResource() {
        return imageResource;
    }

    public boolean isOutOfStock() {
        return outOfStock;
    }
}
