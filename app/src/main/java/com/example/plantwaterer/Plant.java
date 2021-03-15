package com.example.plantwaterer;

public class Plant {
    private int id;
    private String name;
    private String time;
    private String amount;
    private byte[] image;

    public Plant(int id, String name, String time, String amount, byte[] image) {
        this.id = id;
        this.name = name;
        this.time = time;
        this.amount = amount;
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
}
