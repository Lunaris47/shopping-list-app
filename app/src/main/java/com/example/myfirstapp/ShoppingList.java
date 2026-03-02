package com.example.myfirstapp;

import java.util.ArrayList;
import java.util.List;

public class ShoppingList {

    private String title;
    private final List<ShoppingItem> items;

    public ShoppingList(String title) {
        this.title = title;
        this.items = new ArrayList<>();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<ShoppingItem> getItems() {
        return items;
    }

    public void addItem(String name) {
        items.add(new ShoppingItem(name));
    }

    public void removeItem(int index) {
        if (index >= 0 && index < items.size()) {
            items.remove(index);
        }
    }

    public void reorderItems() {
        items.sort((a, b) -> {
            if (a.isChecked() == b.isChecked()) {
                return 0;
            }
            return a.isChecked() ? 1 : -1;
        });
    }
}