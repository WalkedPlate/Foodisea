package com.example.foodisea.entity;

public class Restaurante {
    private String name;
    private String plates;
    private float rating;
    private int imageResource;
    private String location;

    public Restaurante(String name, String plates, float rating, int imageResource, String location) {
        this.name = name;
        this.plates = plates;
        this.rating = rating;
        this.imageResource = imageResource;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public String getPlates() {
        return plates;
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
