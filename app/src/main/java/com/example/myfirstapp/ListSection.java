package com.example.myfirstapp;

import java.util.ArrayList;
import java.util.List;

public class ListSection {

    // null or empty title means this is the default uncategorized section
    private String title;
    private final List<ShoppingItem> items;

    public ListSection(String title) {
        this.title = title;
        this.items = new ArrayList<>();
    }

// -------------------------
// Title
// -------------------------

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isDefaultSection() {
        return title == null || title.trim().isEmpty();
    }

// -------------------------
// Items
// -------------------------

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

// -------------------------
// Reset all items when list recurs
// -------------------------

    public void resetItems() {
        for (ShoppingItem item : items) {
            item.setChecked(false);
        }
    }
}