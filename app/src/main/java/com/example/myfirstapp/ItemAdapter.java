package com.example.myfirstapp;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private final List<ShoppingItem> items;
    private final Runnable onItemChanged;

    // The list name is passed in so it can be shown
    // in the notification when the reminder fires
    private final String listName;

    public ItemAdapter(List<ShoppingItem> items,
                       String listName,
                       Runnable onItemChanged) {
        this.items = items;
        this.listName = listName;
        this.onItemChanged = onItemChanged;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        ShoppingItem item = items.get(position);
        Context context = holder.itemView.getContext();

        // -----------------------------------------------
        // COMPLETED DIVIDER
        // -----------------------------------------------
        boolean showDivider = false;

        if (item.isChecked()) {
            if (position == 0) {
                showDivider = true;
            } else if (!items.get(position - 1).isChecked()) {
                showDivider = true;
            }
        }

        holder.divider.setVisibility(showDivider ? View.VISIBLE : View.GONE);

        // -----------------------------------------------
        // ITEM TEXT + STRIKETHROUGH
        // -----------------------------------------------
        holder.itemText.setText(item.getName());
        holder.itemCheckbox.setOnCheckedChangeListener(null);
        holder.itemCheckbox.setChecked(item.isChecked());

        if (item.isChecked()) {
            holder.itemText.setPaintFlags(
                    holder.itemText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
            );
        } else {
            holder.itemText.setPaintFlags(
                    holder.itemText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG)
            );
        }

        // -----------------------------------------------
        // CHECKBOX
        // -----------------------------------------------
        holder.itemCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setChecked(isChecked);
            notifyItemChanged(position);
            onItemChanged.run();
        });

        // Tap anywhere on card toggles checkbox
        holder.itemView.setOnClickListener(v -> holder.itemCheckbox.toggle());

        // -----------------------------------------------
        // REMINDER BELL ICON
        // Outline = no reminder set
        // Filled + accent color = reminder is set
        // -----------------------------------------------
        if (item.hasReminder()) {
            holder.reminderButton.setImageResource(R.drawable.ic_bell_filled);
            holder.reminderButton.setColorFilter(
                    ContextCompat.getColor(context, R.color.accent_violet));
        } else {
            holder.reminderButton.setImageResource(R.drawable.ic_bell_outline);
            holder.reminderButton.setColorFilter(
                    ContextCompat.getColor(context, R.color.text_secondary));
        }

        // -----------------------------------------------
        // BELL CLICK → SET OR MANAGE REMINDER
        // -----------------------------------------------
        holder.reminderButton.setOnClickListener(v -> {

            if (item.hasReminder()) {
                // Reminder already set — show options to view, change, or cancel
                showReminderOptionsDialog(context, item, position);
            } else {
                // No reminder — go straight to date/time picker
                showDateTimePicker(context, item, position);
            }
        });

        // -----------------------------------------------
        // LONG PRESS → EDIT ITEM NAME
        // -----------------------------------------------
        holder.itemView.setOnLongClickListener(v -> {

            EditText input = new EditText(context);
            input.setText(item.getName());
            input.setSelection(input.getText().length());

            new AlertDialog.Builder(context)
                    .setTitle("Edit Item")
                    .setView(input)
                    .setPositiveButton("Save", (dialog, which) -> {

                        String newName = input.getText().toString().trim();

                        if (!newName.isEmpty()) {
                            item.setName(newName);
                            notifyItemChanged(position);
                            onItemChanged.run();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();

            return true;
        });
    }

    // -----------------------------------------------
    // SHOW OPTIONS WHEN REMINDER ALREADY EXISTS
    // Gives user choice to change or cancel reminder
    // -----------------------------------------------
    private void showReminderOptionsDialog(Context context,
                                           ShoppingItem item,
                                           int position) {

        // Format the existing reminder time for display
        SimpleDateFormat sdf = new SimpleDateFormat(
                "EEE, MMM d yyyy 'at' h:mm a", Locale.getDefault());
        String formattedTime = sdf.format(new Date(item.getReminderTime()));

        String[] options = {"Change Reminder", "Cancel Reminder"};

        new AlertDialog.Builder(context)
                .setTitle("Reminder set for:")
                .setMessage(formattedTime)
                .setItems(options, (dialog, which) -> {

                    if (which == 0) {
                        // Change — show picker again
                        showDateTimePicker(context, item, position);

                    } else if (which == 1) {
                        // Cancel — remove alarm and clear reminder
                        cancelReminder(context, item);
                        item.setReminderTime(0);
                        notifyItemChanged(position);
                        onItemChanged.run();
                        Toast.makeText(context,
                                "Reminder cancelled",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Dismiss", null)
                .show();
    }

    // -----------------------------------------------
    // DATE PICKER → TIME PICKER → SCHEDULE ALARM
    // -----------------------------------------------
    private void showDateTimePicker(Context context,
                                    ShoppingItem item,
                                    int position) {

        Calendar calendar = Calendar.getInstance();

        // If reminder already set, pre-fill with existing time
        if (item.hasReminder()) {
            calendar.setTimeInMillis(item.getReminderTime());
        }

        // Step 1 — Date picker
        DatePickerDialog datePicker = new DatePickerDialog(
                context,
                (view, year, month, dayOfMonth) -> {

                    // Step 2 — Time picker
                    TimePickerDialog timePicker = new TimePickerDialog(
                            context,
                            (timeView, hourOfDay, minute) -> {

                                // Build the chosen date/time
                                Calendar chosen = Calendar.getInstance();
                                chosen.set(year, month, dayOfMonth,
                                        hourOfDay, minute, 0);
                                chosen.set(Calendar.MILLISECOND, 0);

                                long triggerTime = chosen.getTimeInMillis();

                                // Prevent setting a reminder in the past
                                if (triggerTime <= System.currentTimeMillis()) {
                                    Toast.makeText(context,
                                            "Please choose a future date and time",
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // Cancel any existing alarm for this item
                                cancelReminder(context, item);

                                // Save the new reminder time
                                item.setReminderTime(triggerTime);

                                // Schedule the alarm
                                scheduleReminder(context, item, triggerTime);

                                notifyItemChanged(position);
                                onItemChanged.run();

                                // Confirm to user
                                SimpleDateFormat sdf = new SimpleDateFormat(
                                        "EEE, MMM d 'at' h:mm a",
                                        Locale.getDefault());
                                Toast.makeText(context,
                                        "Reminder set for " +
                                                sdf.format(new Date(triggerTime)),
                                        Toast.LENGTH_LONG).show();
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            false
                    );

                    timePicker.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Prevent picking dates in the past
        datePicker.getDatePicker().setMinDate(System.currentTimeMillis());
        datePicker.show();
    }

    // -----------------------------------------------
// SCHEDULE ALARM VIA ALARMMANAGER
// Uses item name hashCode as unique request code
// so each item has its own independent alarm
// -----------------------------------------------
    private void scheduleReminder(Context context,
                                  ShoppingItem item,
                                  long triggerTime) {

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) return;

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("item_name", item.getName());
        intent.putExtra("list_name", listName);

        int requestCode = (item.getName() + triggerTime).hashCode();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Check if exact alarms are permitted before calling
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                triggerTime,
                                pendingIntent
                        );
                    } else {
                        // Fallback — less precise but won't crash
                        alarmManager.setAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                triggerTime,
                                pendingIntent
                        );
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                    );
                }
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                );
            }
        } catch (SecurityException e) {
            // Last resort fallback — should not reach here with USE_EXACT_ALARM
            // but prevents any crash if it does
            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
            );
        }
    }


    // -----------------------------------------------
    // CANCEL AN EXISTING ALARM
    // Must use same request code as when it was set
    // -----------------------------------------------
    private void cancelReminder(Context context, ShoppingItem item) {

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, ReminderReceiver.class);

        int requestCode = (item.getName() + item.getReminderTime()).hashCode();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        View divider;
        TextView itemText;
        CheckBox itemCheckbox;
        ImageButton reminderButton;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemText = itemView.findViewById(R.id.itemText);
            itemCheckbox = itemView.findViewById(R.id.itemCheckbox);
            divider = itemView.findViewById(R.id.completedDivider);
            reminderButton = itemView.findViewById(R.id.reminderButton);
        }
    }
}
