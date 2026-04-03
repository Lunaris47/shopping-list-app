package com.example.myfirstapp;

public class ShoppingItem {

    private String name;
    private boolean checked;

    // Reminder time stored as milliseconds since epoch
    // 0 means no reminder is set
    private long reminderTime = 0;

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
}
