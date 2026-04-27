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
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // -----------------------------------------------
// VIEW TYPES
// -----------------------------------------------
    private static final int TYPE_SECTION_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_SECTION_FOOTER = 2;

    // -----------------------------------------------
// FLAT LIST ENTRY
// -----------------------------------------------
    private static class Entry {
        static final int KIND_HEADER = 0;
        static final int KIND_ITEM = 1;
        static final int KIND_FOOTER = 2;

        int kind;
        ListSection section;
        ShoppingItem item;
        int sectionIndex;
        int itemIndexInSection;

        static Entry header(ListSection section, int sectionIndex) {
            Entry e = new Entry();
            e.kind = KIND_HEADER;
            e.section = section;
            e.sectionIndex = sectionIndex;
            return e;
        }

        static Entry item(ShoppingItem item, ListSection section,
                          int sectionIndex, int itemIndexInSection) {
            Entry e = new Entry();
            e.kind = KIND_ITEM;
            e.item = item;
            e.section = section;
            e.sectionIndex = sectionIndex;
            e.itemIndexInSection = itemIndexInSection;
            return e;
        }

        static Entry footer(ListSection section, int sectionIndex) {
            Entry e = new Entry();
            e.kind = KIND_FOOTER;
            e.section = section;
            e.sectionIndex = sectionIndex;
            return e;
        }
    }

    private final List<ListSection> sections;
    private List<Entry> flatList;
    private final String listName;
    private final Runnable onItemChanged;
    private final boolean isReadOnly;

    public interface OnSectionDeletedListener {
        void onSectionDeleted(ListSection section, int sectionIndex);
    }

    private final OnSectionDeletedListener sectionDeletedListener;

    public ItemAdapter(List<ListSection> sections,
                       String listName,
                       boolean isReadOnly,
                       Runnable onItemChanged,
                       OnSectionDeletedListener sectionDeletedListener) {
        this.sections = sections;
        this.listName = listName;
        this.isReadOnly = isReadOnly;
        this.onItemChanged = onItemChanged;
        this.sectionDeletedListener = sectionDeletedListener;
        this.flatList = buildFlatList();
    }

    // -----------------------------------------------
// REBUILD FLAT LIST
// -----------------------------------------------
    private List<Entry> buildFlatList() {
        List<Entry> list = new ArrayList<>();
        for (int s = 0; s < sections.size(); s++) {
            ListSection section = sections.get(s);

            // Show header only for named sections
            if (!section.isDefaultSection()) {
                list.add(Entry.header(section, s));
            }

            // Unchecked items first
            List<ShoppingItem> items = section.getItems();
            for (int i = 0; i < items.size(); i++) {
                if (!items.get(i).isChecked()) {
                    list.add(Entry.item(items.get(i), section, s, i));
                }
            }

            // Checked items after
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).isChecked()) {
                    list.add(Entry.item(items.get(i), section, s, i));
                }
            }

            // Footer — tap to add item — only shown in edit mode
            if (!isReadOnly) {
                list.add(Entry.footer(section, s));
            }
        }
        return list;
    }

    public void refreshList() {
        flatList = buildFlatList();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        int kind = flatList.get(position).kind;
        if (kind == Entry.KIND_HEADER) return TYPE_SECTION_HEADER;
        if (kind == Entry.KIND_FOOTER) return TYPE_SECTION_FOOTER;
        return TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                      int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_SECTION_HEADER) {
            View view = inflater.inflate(
                    R.layout.list_section_header, parent, false);
            return new SectionViewHolder(view);
        } else if (viewType == TYPE_SECTION_FOOTER) {
            View view = inflater.inflate(
                    R.layout.list_section_footer, parent, false);
            return new FooterViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.list_item, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder,
                                 int position) {
        Entry entry = flatList.get(position);

        if (entry.kind == Entry.KIND_HEADER) {
            bindSectionHeader((SectionViewHolder) holder, entry);
        } else if (entry.kind == Entry.KIND_FOOTER) {
            bindSectionFooter((FooterViewHolder) holder, entry);
        } else {
            bindItem((ItemViewHolder) holder, entry);
        }
    }

    // -----------------------------------------------
// BIND SECTION HEADER
// -----------------------------------------------
    private void bindSectionHeader(SectionViewHolder holder, Entry entry) {
        ListSection section = entry.section;
        holder.sectionTitle.setText(section.getTitle());

        holder.sectionTitle.setOnLongClickListener(v -> {
            Context context = v.getContext();
            EditText input = new EditText(context);
            input.setText(section.getTitle());
            input.setSelection(input.getText().length());

            new AlertDialog.Builder(context)
                    .setTitle("Rename Section")
                    .setView(input)
                    .setPositiveButton("Save", (dialog, which) -> {
                        String newName = input.getText().toString().trim();
                        if (!newName.isEmpty()) {
                            section.setTitle(newName);
                            refreshList();
                            onItemChanged.run();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();

            return true;
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (sectionDeletedListener != null) {
                sectionDeletedListener.onSectionDeleted(
                        section, entry.sectionIndex);
            }
        });
    }

    // -----------------------------------------------
// BIND SECTION FOOTER
// Tap to add item to this section
// -----------------------------------------------
    private void bindSectionFooter(FooterViewHolder holder, Entry entry) {
        ListSection section = entry.section;

        holder.addItemFooter.setOnClickListener(v -> {
            Context context = v.getContext();
            EditText input = new EditText(context);
            input.setHint("Item name");
            input.setSingleLine(true);

            new AlertDialog.Builder(context)
                    .setTitle("Add Item")
                    .setView(input)
                    .setPositiveButton("Add", (dialog, which) -> {
                        String name = input.getText().toString().trim();
                        if (!name.isEmpty()) {
                            section.addItem(name);
                            refreshList();
                            onItemChanged.run();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    // -----------------------------------------------
// BIND ITEM ROW
// -----------------------------------------------
    private void bindItem(ItemViewHolder holder, Entry entry) {
        ShoppingItem item = entry.item;
        Context context = holder.itemView.getContext();

        // Completed divider
        int flatPos = flatList.indexOf(entry);
        boolean showDivider = false;

        if (item.isChecked()) {
            if (flatPos == 0) {
                showDivider = true;
            } else {
                Entry prev = flatList.get(flatPos - 1);
                if (prev.kind == Entry.KIND_HEADER) {
                    showDivider = false;
                } else if (prev.kind == Entry.KIND_ITEM && !prev.item.isChecked()) {
                    showDivider = true;
                }
            }
        }

        holder.divider.setVisibility(showDivider ? View.VISIBLE : View.GONE);

        // Item text + strikethrough + gray out
        holder.itemText.setText(item.getName());

        if (item.isChecked()) {
            holder.itemCheckbox.setImageResource(R.drawable.ic_circle_filled);
            holder.itemText.setPaintFlags(
                    holder.itemText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.itemText.setAlpha(0.4f);
        } else {
            holder.itemCheckbox.setImageResource(R.drawable.ic_circle_outline);
            holder.itemText.setPaintFlags(
                    holder.itemText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.itemText.setAlpha(1.0f);
        }

        holder.itemView.setOnClickListener(v -> {
            item.setChecked(!item.isChecked());
            refreshList();
            onItemChanged.run();
        });

        // Reminder bell
        if (item.hasReminder()) {
            holder.reminderButton.setImageResource(R.drawable.ic_bell_filled);
            holder.reminderButton.setColorFilter(
                    ContextCompat.getColor(context, R.color.accent_violet));
        } else {
            holder.reminderButton.setImageResource(R.drawable.ic_bell_outline);
            holder.reminderButton.setColorFilter(
                    ContextCompat.getColor(context, R.color.text_secondary));
        }

        holder.reminderButton.setOnClickListener(v -> {
            if (item.hasReminder()) {
                showReminderOptionsDialog(context, item);
            } else {
                showReminderDialog(context, item);
            }
        });

        // Long press to edit item name
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
                            refreshList();
                            onItemChanged.run();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();

            return true;
        });
    }

    // -----------------------------------------------
// REMINDER DIALOGS
// -----------------------------------------------
    private void showReminderOptionsDialog(Context context, ShoppingItem item) {
        SimpleDateFormat sdf = new SimpleDateFormat(
                "EEE, MMM d yyyy 'at' h:mm a", Locale.getDefault());
        String formattedTime = sdf.format(new Date(item.getReminderTime()));

        String[] options = {"Change Reminder", "Cancel Reminder"};

        new AlertDialog.Builder(context)
                .setTitle("Reminder: " + formattedTime)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showReminderDialog(context, item);
                    } else if (which == 1) {
                        cancelReminder(context, item);
                        item.setReminderTime(0);
                        refreshList();
                        onItemChanged.run();
                        Toast.makeText(context,
                                "Reminder cancelled",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Dismiss", null)
                .show();
    }

    private void showReminderDialog(Context context, ShoppingItem item) {

        class ReminderState {
            int year = -1;
            int month = -1;
            int day = -1;
            int hour = -1;
            int minute = -1;
        }

        final ReminderState state = new ReminderState();

        if (item.hasReminder()) {
            Calendar existing = Calendar.getInstance();
            existing.setTimeInMillis(item.getReminderTime());
            state.year = existing.get(Calendar.YEAR);
            state.month = existing.get(Calendar.MONTH);
            state.day = existing.get(Calendar.DAY_OF_MONTH);
            state.hour = existing.get(Calendar.HOUR_OF_DAY);
            state.minute = existing.get(Calendar.MINUTE);
        }

        SimpleDateFormat dateFmt = new SimpleDateFormat(
                "EEE, MMM d yyyy", Locale.getDefault());
        SimpleDateFormat timeFmt = new SimpleDateFormat(
                "h:mm a", Locale.getDefault());

        android.widget.LinearLayout layout =
                new android.widget.LinearLayout(context);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 16);

        final TextView dateTextView = new TextView(context);
        dateTextView.setText(state.year == -1
                ? "No date set — tap to choose"
                : dateFmt.format(buildCalendar(
                state.year, state.month, state.day, 0, 0).getTime()));
        dateTextView.setTextSize(15);
        dateTextView.setPadding(0, 16, 0, 16);
        dateTextView.setTextColor(
                ContextCompat.getColor(context, R.color.accent_violet_light));

        final TextView timeTextView = new TextView(context);
        timeTextView.setText(state.hour == -1
                ? "No time set — optional"
                : timeFmt.format(buildCalendar(
                state.year, state.month, state.day,
                state.hour, state.minute).getTime()));
        timeTextView.setTextSize(15);
        timeTextView.setPadding(0, 16, 0, 16);
        timeTextView.setTextColor(
                ContextCompat.getColor(context, R.color.text_secondary));

        layout.addView(dateTextView);
        layout.addView(timeTextView);

        AlertDialog reminderDialog = new AlertDialog.Builder(context)
                .setTitle("Set Reminder for: " + item.getName())
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {

                    if (state.year == -1) {
                        Toast.makeText(context,
                                "Please choose a date first",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int hour = state.hour == -1 ? 9 : state.hour;
                    int minute = state.minute == -1 ? 0 : state.minute;

                    Calendar chosen = buildCalendar(
                            state.year, state.month, state.day, hour, minute);

                    long triggerTime = chosen.getTimeInMillis();

                    if (triggerTime <= System.currentTimeMillis()) {
                        Toast.makeText(context,
                                "Please choose a future date and time",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    cancelReminder(context, item);
                    item.setReminderTime(triggerTime);
                    scheduleReminder(context, item, triggerTime);
                    refreshList();
                    onItemChanged.run();

                    SimpleDateFormat confirmFmt = new SimpleDateFormat(
                            "EEE, MMM d 'at' h:mm a", Locale.getDefault());
                    Toast.makeText(context,
                            "Reminder set for " +
                                    confirmFmt.format(new Date(triggerTime)),
                            Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Cancel", null)
                .create();

        dateTextView.setOnClickListener(v -> {

            Calendar cal = Calendar.getInstance();
            if (state.year != -1) {
                cal.set(state.year, state.month, state.day);
            }

            DatePickerDialog datePicker = new DatePickerDialog(
                    context,
                    (dateView, year, month, dayOfMonth) -> {
                        state.year = year;
                        state.month = month;
                        state.day = dayOfMonth;

                        Calendar selected = buildCalendar(
                                year, month, dayOfMonth, 0, 0);
                        dateTextView.setText(
                                dateFmt.format(selected.getTime()));
                        dateTextView.setTextColor(
                                ContextCompat.getColor(
                                        context, R.color.accent_violet));
                        timeTextView.setTextColor(
                                ContextCompat.getColor(
                                        context, R.color.accent_violet_light));
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH));

            datePicker.getDatePicker().setMinDate(System.currentTimeMillis());
            datePicker.show();
        });

        timeTextView.setOnClickListener(v -> {

            if (state.year == -1) {
                Toast.makeText(context,
                        "Please choose a date first",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            int hour = state.hour == -1 ? 9 : state.hour;
            int minute = state.minute == -1 ? 0 : state.minute;

            TimePickerDialog timePicker = new TimePickerDialog(
                    context,
                    (timeView, hourOfDay, min) -> {
                        state.hour = hourOfDay;
                        state.minute = min;

                        Calendar selected = buildCalendar(
                                state.year, state.month, state.day,
                                hourOfDay, min);
                        timeTextView.setText(
                                timeFmt.format(selected.getTime()));
                        timeTextView.setTextColor(
                                ContextCompat.getColor(
                                        context, R.color.accent_violet));
                    },
                    hour, minute, false);

            timePicker.show();
        });

        reminderDialog.show();
    }

    private Calendar buildCalendar(int year, int month, int day,
                                   int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day, hour, minute, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    // -----------------------------------------------
// ALARM MANAGER
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
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                triggerTime,
                                pendingIntent);
                    } else {
                        alarmManager.setAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                triggerTime,
                                pendingIntent);
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent);
                }
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent);
            }
        } catch (SecurityException e) {
            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent);
        }
    }

    private void cancelReminder(Context context, ShoppingItem item) {
        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, ReminderReceiver.class);

        int requestCode = (item.getName() + item.getReminderTime()).hashCode();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    // -----------------------------------------------
// SWIPE TO DELETE
// -----------------------------------------------
    public SwipeDeleteResult deleteAtPosition(int position) {
        Entry entry = flatList.get(position);
        if (entry.kind != Entry.KIND_ITEM) return null;

        ShoppingItem deletedItem = entry.item;
        ListSection section = entry.section;
        int itemIndex = section.getItems().indexOf(deletedItem);

        section.getItems().remove(deletedItem);
        refreshList();
        onItemChanged.run();

        return new SwipeDeleteResult(deletedItem, section, itemIndex);
    }

    public boolean isSwipeable(int position) {
        if (position < 0 || position >= flatList.size()) return false;
        return flatList.get(position).kind == Entry.KIND_ITEM;
    }

    public static class SwipeDeleteResult {
        public final ShoppingItem item;
        public final ListSection section;
        public final int indexInSection;

        public SwipeDeleteResult(ShoppingItem item,
                                 ListSection section,
                                 int indexInSection) {
            this.item = item;
            this.section = section;
            this.indexInSection = indexInSection;
        }
    }

    @Override
    public int getItemCount() {
        return flatList.size();
    }

    // -----------------------------------------------
// VIEW HOLDERS
// -----------------------------------------------
    static class SectionViewHolder extends RecyclerView.ViewHolder {
        TextView sectionTitle;
        ImageButton deleteButton;

        SectionViewHolder(@NonNull View itemView) {
            super(itemView);
            sectionTitle = itemView.findViewById(R.id.sectionTitle);
            deleteButton = itemView.findViewById(R.id.deleteSectionButton);
        }
    }

    static class FooterViewHolder extends RecyclerView.ViewHolder {
        TextView addItemFooter;

        FooterViewHolder(@NonNull View itemView) {
            super(itemView);
            addItemFooter = itemView.findViewById(R.id.addItemFooter);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        View divider;
        TextView itemText;
        ImageView itemCheckbox;
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