package com.example.foodisea.entity;

public class Restaurante {
    private String name;
    private String plates;
    private float rating;
    private int imageResource;

    public Restaurante(String name, String plates, float rating, int imageResource) {
        this.name = name;
        this.plates = plates;
        this.rating = rating;
        this.imageResource = imageResource;
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
}
