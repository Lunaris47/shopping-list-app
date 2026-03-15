package com.example.myfirstapp;

import java.util.ArrayList;
import java.util.List;

public class ShoppingList {

    private String title;
    private final List<ShoppingItem> items;

    // Indicates whether the list is hidden from the main screen
    private boolean archived = false;

    // Recurring configuration
    // none, weekly, monthly, yearly
    private String recurringType = "none";

    // Stores the recurrence value
    // weekly  → Sunday
    // monthly → 15
    // yearly  → 03-25
    private String recurringValue = "";

    // Prevents multiple restores in one day
    private String lastRestoredDate = "";

    public ShoppingList(String title) {
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

    // Moves checked items to the bottom
    public void reorderItems() {
        items.sort((a, b) -> {
            if (a.isChecked() == b.isChecked()) {
                return 0;
            }
            return a.isChecked() ? 1 : -1;
        });
    }

    // -------------------------
    // Archived State
    // -------------------------

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    // -------------------------
    // Recurring Configuration
    // -------------------------

    public String getRecurringType() {
        return recurringType;
    }

    public void setRecurringType(String type) {
        this.recurringType = type;
    }

    public String getRecurringValue() {
        return recurringValue;
    }

    public void setRecurringValue(String value) {
        this.recurringValue = value;
    }

    // -------------------------
    // Recurring Restore Tracking
    // -------------------------

    public String getLastRestoredDate() {
        return lastRestoredDate;
    }

    public void setLastRestoredDate(String date) {
        this.lastRestoredDate = date;
    }

    // -------------------------
    // Reset items when list recurs
    // -------------------------

    public void resetItems() {
        for (ShoppingItem item : items) {
            item.setChecked(false);
        }
    }
}