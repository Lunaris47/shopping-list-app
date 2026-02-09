package com.example.myfirstapp;

import java.util.ArrayList;
import java.util.List;

public class ShoppingList {

    private final String title;
    private final List<String> items;

    public ShoppingList(String title) {
        this.title = title;
        this.items = new ArrayList<>();
    }

    public String getTitle() {
        return title;
    }

    public List<String> getItems() {
        return items;
    }
}
