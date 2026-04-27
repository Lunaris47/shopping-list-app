package com.example.myfirstapp;

import java.util.ArrayList;
import java.util.List;

public class ShoppingList {

    private String title;

    // Sections replace the flat items list
// Option C — default section always exists for uncategorized items
    private List<ListSection> sections;

    // Indicates whether the list is hidden from the main screen
    private boolean archived = false;

    // Indicates whether the list is favorited
    private boolean favorite = false;

    // Recurring configuration
    private String recurringType = "none";
    private String recurringValue = "";
    private String lastRestoredDate = "";

    public ShoppingList(String title) {
        this.title = title;
        this.sections = new ArrayList<>();
        // Always start with one default uncategorized section
        this.sections.add(new ListSection(""));
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
// Sections
// -------------------------

    public List<ListSection> getSections() {
        if (sections == null) {
            sections = new ArrayList<>();
            sections.add(new ListSection(""));
        }
        return sections;
    }

    public void addSection(String sectionTitle) {
        getSections().add(new ListSection(sectionTitle));
    }

    public void removeSection(int index) {
        if (index >= 0 && index < getSections().size()) {
            getSections().remove(index);
        }
    }

// -------------------------
// Backward compatibility
// Returns ALL items across ALL sections
// Used by card preview in ListAdapter
// -------------------------

    public List<ShoppingItem> getItems() {
        List<ShoppingItem> allItems = new ArrayList<>();
        for (ListSection section : getSections()) {
            allItems.addAll(section.getItems());
        }
        return allItems;
    }

    // Adds item to the default section (first section)
// Used for backward compatibility with existing save data
    public void addItem(String name) {
        getSections().get(0).addItem(name);
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
// Favorite State
// -------------------------

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
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
// Reset all items across all sections when list recurs
// -------------------------

    public void resetItems() {
        for (ListSection section : getSections()) {
            section.resetItems();
        }
    }
}