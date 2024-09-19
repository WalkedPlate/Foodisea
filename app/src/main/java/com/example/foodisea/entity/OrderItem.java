package com.example.foodisea.entity;

public class OrderItem {
    private String name;
    private String size;
    private int quantity;
    private int imageResId;

    public OrderItem(String name, String size, int quantity, int imageResId) {
        this.name = name;
        this.size = size;
        this.quantity = quantity;
        this.imageResId = imageResId;
    }

    public String getName() { return name; }
    public String getSize() { return size; }
    public int getQuantity() { return quantity; }
    public int getImageResId() { return imageResId; }
}
