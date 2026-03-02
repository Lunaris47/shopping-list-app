package com.example.myfirstapp;

public class ShoppingItem {

    private String name;
    private boolean checked;

    public ShoppingItem(String name) {
        this.name = name;
        this.checked = false;
    }

    public String getName() {
        return name;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public void toggleChecked() {
        this.checked = !this.checked;
    }
}