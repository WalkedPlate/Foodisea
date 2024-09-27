package com.example.foodisea.entity;

public class Restaurante {
    private String name;
    private String categories;
    private float rating;
    private int imageResource;
    private String location;

    public Restaurante(String name, String categories, float rating, int imageResource, String location) {
        this.name = name;
        this.categories = categories;
        this.rating = rating;
        this.imageResource = imageResource;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public String getCategories() {
        return categories;
    }

    public float getRating() {
        return rating;
    }

    public int getImageResource() {
        return imageResource;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
