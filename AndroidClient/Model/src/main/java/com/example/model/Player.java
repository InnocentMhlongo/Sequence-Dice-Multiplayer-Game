package com.example.model;
import java.io.Serializable;

public class Player implements Serializable {
    private final int id;
    private String name;
    private String colour;

    public Player(int id, String name, String colour) {
        this.id = id;
        this.name = name;
        this.colour = colour;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColour() {
        return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    public int getId() {
        return id;
    }
}