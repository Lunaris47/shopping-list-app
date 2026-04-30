package com.example.myfirstapp;

public class ShoppingItem {

    private String name;
    private boolean checked;

    // Reminder time stored as milliseconds since epoch
    // 0 means no reminder is set
    private long reminderTime = 0;
    private String recurringType = "none";
    private String recurringValue = "";
    private String lastRestoredDate = "";

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

    public void setName(String name) {
        this.name = name;
    }

    public void toggleChecked() {
        this.checked = !this.checked;
    }

    // -------------------------
    // Reminder
    // -------------------------

    public long getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(long reminderTime) {
        this.reminderTime = reminderTime;
    }

    public boolean hasReminder() {
        return reminderTime > 0;
    }

    // -------------------------
// Recurring Configuration
// -------------------------

    public String getRecurringType() {
        return recurringType != null ? recurringType : "none";
    }

    public void setRecurringType(String type) {
        this.recurringType = type;
    }

    public String getRecurringValue() {
        return recurringValue != null ? recurringValue : "";
    }

    public void setRecurringValue(String value) {
        this.recurringValue = value;
    }

// -------------------------
// Recurring Restore Tracking
// -------------------------

    public String getLastRestoredDate() {
        return lastRestoredDate != null ? lastRestoredDate : "";
    }

    public void setLastRestoredDate(String date) {
        this.lastRestoredDate = date;
    }

// -------------------------
// Reset just this item when it recurs
// -------------------------

    public boolean hasRecurrence() {
        return recurringType != null
                && !recurringType.equals("none")
                && !recurringType.isEmpty();
    }
}
