package com.example.myfirstapp;

import java.util.ArrayList;
import java.util.List;

public class ShoppingList {

    private String title;
    private final List<ShoppingItem> items;
    private boolean archived = false;
    private boolean recurring = false;
    private String recurringDay = "";
    private String lastRestoredDate = "";

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

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public boolean isRecurring() {
        return recurring;
    }

    public void setRecurring(boolean recurring) {
        this.recurring = recurring;
    }

    public String getRecurringDay() {
        return recurringDay;
    }

    public void setRecurringDay(String day) {
        this.recurringDay = day;
    }

    public void reorderItems() {
        items.sort((a, b) -> {
            if (a.isChecked() == b.isChecked()) {
                return 0;
            }
            return a.isChecked() ? 1 : -1;
        });
    }

    public String getLastRestoredDate() {
        return lastRestoredDate;
    }

    public void setLastRestoredDate(String date) {
        this.lastRestoredDate = date;
    }

    public void resetItems() {
        for (ShoppingItem item : items) {
            item.setChecked(false);
        }
    }
}