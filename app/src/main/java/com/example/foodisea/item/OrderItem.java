package com.example.foodisea.item;

public class OrderItem {
    private String name;
    private double price;
    private int quantity;
    private String imageUrl;  // URL de la imagen en Firebase Storage

    public OrderItem(String name, double price, int quantity, String imageUrl) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
    }

    // Getters
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getImageUrl() { return imageUrl; }
}