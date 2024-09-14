package com.example.foodisea.entity;

public class Pedido {
    private String orderNumber;
    private String customerName;
    private String address;
    private double price;

    public Pedido(String orderNumber, String customerName, String address, double price) {
        this.orderNumber = orderNumber;
        this.customerName = customerName;
        this.address = address;
        this.price = price;
    }

    // Getters
    public String getOrderNumber() { return orderNumber; }
    public String getCustomerName() { return customerName; }
    public String getAddress() { return address; }
    public double getPrice() { return price; }
}
