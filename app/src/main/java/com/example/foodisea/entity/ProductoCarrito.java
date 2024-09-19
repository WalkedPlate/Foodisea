package com.example.foodisea.entity;

public class ProductoCarrito {
    private String productName;
    private double productPrice;
    private int imageResource;
    private int quantity;

    public ProductoCarrito(String productName, double productPrice, int imageResource, int quantity) {
        this.productName = productName;
        this.productPrice = productPrice;
        this.imageResource = imageResource;
        this.quantity = quantity;
    }

    public String getProductName() {
        return productName;
    }
    public double getProductPrice() {
        return productPrice;
    }
    public int getImageResource() {
        return imageResource;
    }

    public int getQuantity() {
        return quantity;
    }
}

