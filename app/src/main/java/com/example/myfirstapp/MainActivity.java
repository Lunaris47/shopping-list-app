package com.example.myfirstapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final List<ShoppingList> shoppingLists = new ArrayList<>();
    private final List<ShoppingList> visibleLists = new ArrayList<>();
    private ListAdapter adapter;
    private RecyclerView recyclerView;

    private static final String PREFS_NAME = "shopping_prefs";
    private static final String LIST_KEY = "shopping_lists";
    private static final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));

        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        1001);
            }
        }

        recyclerView = findViewById(R.id.recyclerView);
        FloatingActionButton fab = findViewById(R.id.fabAddList);

        loadData();
        checkRecurringLists();
        refreshVisibleLists();

        if (shoppingLists.isEmpty()) {
            shoppingLists.add(new ShoppingList("Groceries"));
            shoppingLists.add(new ShoppingList("Homework"));
            shoppingLists.add(new ShoppingList("Chores"));
            shoppingLists.add(new ShoppingList("Packing"));
            saveData(this);
        }

        adapter = new ListAdapter(
                visibleLists,
                position -> {
                    ShoppingList selectedList = visibleLists.get(position);
                    showListOptions(selectedList);
                },
                () -> {
                    refreshVisibleLists();
                    saveData(this);
                }
        );

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);

        ItemTouchHelper.Callback callback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN |
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT,
                0) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                adapter.onItemMoved(
                        viewHolder.getAdapterPosition(),
                        target.getAdapterPosition()
                );
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder,
                                 int direction) {
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                adapter.onItemDropped();
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return false;
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        adapter.setItemTouchHelper(itemTouchHelper);

        fab.setOnClickListener(v -> {

            EditText input = new EditText(this);
            input.setHint("List name");
            input.setSingleLine(true);

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Create New List")
                    .setView(input)
                    .setPositiveButton("Create", (d, which) -> {
                        String name = input.getText().toString().trim();
                        if (!name.isEmpty()) {
                            shoppingLists.add(new ShoppingList(name));
                            refreshVisibleLists();
                            saveData(this);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .create();

            dialog.show();

            input.setOnEditorActionListener((textView, actionId, event) -> {
                String name = input.getText().toString().trim();
                if (!name.isEmpty()) {
                    shoppingLists.add(new ShoppingList(name));
                    refreshVisibleLists();
                    saveData(this);
                    dialog.dismiss();
                }
                return true;
            });
        });
    }

    // --------------------------------------------------
// LONG PRESS MENU
// --------------------------------------------------
    private void showListOptions(ShoppingList selectedList) {

        String[] options = {"Set Recurrence", "Archive List", "Delete List"};

        new AlertDialog.Builder(this)
                .setTitle("List Options")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showRecurrenceDialog(selectedList);
                    } else if (which == 1) {
                        archiveList(selectedList);
                    } else if (which == 2) {
                        deleteList(selectedList);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // --------------------------------------------------
// SET RECURRENCE
// --------------------------------------------------
    private void showRecurrenceDialog(ShoppingList selectedList) {

        class RecurrenceState {
            int selectedOption = 0;
            String selectedDay = "";
            String selectedYearlyDate = "";
        }

        final RecurrenceState state = new RecurrenceState();

        String currentType = selectedList.getRecurringType();
        if (currentType != null) {
            switch (currentType) {
                case "weekly":  state.selectedOption = 1; break;
                case "monthly": state.selectedOption = 2; break;
                case "yearly":  state.selectedOption = 3; break;
                default:        state.selectedOption = 0; break;
            }
        }

        state.selectedDay = selectedList.getRecurringValue() != null
                ? selectedList.getRecurringValue() : "";

        // Pre fill yearly date if already set
        if (state.selectedOption == 3 && !state.selectedDay.isEmpty()) {
            state.selectedYearlyDate = state.selectedDay;
        }

        final String[] weekDays = {
                "Sunday", "Monday", "Tuesday",
                "Wednesday", "Thursday", "Friday", "Saturday"
        };

        final String[] monthDays = {
                "1","2","3","4","5","6","7","8","9","10",
                "11","12","13","14","15","16","17","18","19","20",
                "21","22","23","24","25","26","27","28","29","30",
                "31"
        };

        android.widget.LinearLayout layout =
                new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(48, 16, 48, 8);

        android.widget.RadioGroup radioGroup =
                new android.widget.RadioGroup(this);
        radioGroup.setOrientation(android.widget.RadioGroup.VERTICAL);

        String[] options = {
                "No Recurrence",
                "Weekly",
                "Monthly",
                "Yearly",
                "Holiday / Special Date (Coming Soon)"
        };

        for (int i = 0; i < options.length; i++) {
            android.widget.RadioButton rb =
                    new android.widget.RadioButton(this);
            rb.setText(options[i]);
            rb.setId(i);
            rb.setTextSize(15);
            rb.setPadding(0, 12, 0, 12);

            if (i == 4) {
                rb.setEnabled(false);
                rb.setAlpha(0.4f);
            }

            if (i == state.selectedOption) {
                rb.setChecked(true);
            }

            radioGroup.addView(rb);
        }

        final android.widget.LinearLayout subOptionLayout =
                new android.widget.LinearLayout(this);
        subOptionLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
        subOptionLayout.setPadding(32, 8, 0, 8);
        subOptionLayout.setVisibility(android.view.View.GONE);

        final android.widget.TextView subOptionLabel =
                new android.widget.TextView(this);
        subOptionLabel.setTextSize(13);
        subOptionLabel.setPadding(0, 4, 0, 4);
        subOptionLabel.setTextColor(
                androidx.core.content.ContextCompat.getColor(
                        this, R.color.text_secondary));

        final android.widget.Spinner subOptionSpinner =
                new android.widget.Spinner(this);

        // Yearly date picker button shown instead of spinner for yearly
        final android.widget.Button yearlyDateButton =
                new android.widget.Button(this);
        yearlyDateButton.setVisibility(android.view.View.GONE);

        subOptionLayout.addView(subOptionLabel);
        subOptionLayout.addView(subOptionSpinner);
        subOptionLayout.addView(yearlyDateButton);

        final android.widget.TextView previewLabel =
                new android.widget.TextView(this);
        previewLabel.setTextSize(12);
        previewLabel.setPadding(0, 16, 0, 4);
        previewLabel.setTextColor(
                androidx.core.content.ContextCompat.getColor(
                        this, R.color.accent_violet_light));
        previewLabel.setText(
                "Current: " + getCurrentRecurrenceDescription(selectedList));

        layout.addView(radioGroup);
        layout.addView(subOptionLayout);
        layout.addView(previewLabel);

        android.widget.ArrayAdapter<String> weekAdapter =
                new android.widget.ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        weekDays);
        weekAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        android.widget.ArrayAdapter<String> monthAdapter =
                new android.widget.ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        monthDays);
        monthAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        android.widget.AdapterView.OnItemSelectedListener spinnerListener =
                new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            android.view.View view,
                            int position,
                            long id) {
                        state.selectedDay =
                                (String) parent.getItemAtPosition(position);
                    }
                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent) {}
                };

        subOptionSpinner.setOnItemSelectedListener(spinnerListener);

        // Helper to open yearly date picker
        Runnable openYearlyPicker = () -> {
            java.util.Calendar cal = java.util.Calendar.getInstance();

            // Pre fill with existing yearly date if set
            if (!state.selectedYearlyDate.isEmpty()) {
                try {
                    java.text.SimpleDateFormat parseFmt =
                            new java.text.SimpleDateFormat("MM-dd",
                                    java.util.Locale.getDefault());
                    java.util.Date parsed = parseFmt.parse(state.selectedYearlyDate);
                    if (parsed != null) {
                        java.util.Calendar temp = java.util.Calendar.getInstance();
                        temp.setTime(parsed);
                        cal.set(java.util.Calendar.MONTH, temp.get(java.util.Calendar.MONTH));
                        cal.set(java.util.Calendar.DAY_OF_MONTH,
                                temp.get(java.util.Calendar.DAY_OF_MONTH));
                    }
                } catch (java.text.ParseException e) {
                    // use today as fallback
                }
            }

            android.app.DatePickerDialog picker = new android.app.DatePickerDialog(
                    this,
                    (dateView, year, month, dayOfMonth) -> {
                        java.util.Calendar picked = java.util.Calendar.getInstance();
                        picked.set(year, month, dayOfMonth);

                        java.text.SimpleDateFormat saveFmt =
                                new java.text.SimpleDateFormat("MM-dd",
                                        java.util.Locale.getDefault());
                        java.text.SimpleDateFormat displayFmt =
                                new java.text.SimpleDateFormat("MMMM d",
                                        java.util.Locale.getDefault());

                        state.selectedYearlyDate = saveFmt.format(picked.getTime());
                        String display = displayFmt.format(picked.getTime());

                        yearlyDateButton.setText("Selected: " + display);
                        previewLabel.setText("Will be set to: Every year on " + display);
                    },
                    cal.get(java.util.Calendar.YEAR),
                    cal.get(java.util.Calendar.MONTH),
                    cal.get(java.util.Calendar.DAY_OF_MONTH));

            picker.show();
        };

        yearlyDateButton.setOnClickListener(v -> openYearlyPicker.run());

        // Show correct sub option if recurrence already set when dialog opens
        if (state.selectedOption == 1) {
            subOptionLabel.setText("Repeat every week on:");
            subOptionSpinner.setAdapter(weekAdapter);
            subOptionSpinner.setVisibility(android.view.View.VISIBLE);
            yearlyDateButton.setVisibility(android.view.View.GONE);
            for (int i = 0; i < weekDays.length; i++) {
                if (weekDays[i].equals(state.selectedDay)) {
                    subOptionSpinner.setSelection(i);
                    break;
                }
            }
            subOptionLayout.setVisibility(android.view.View.VISIBLE);
        } else if (state.selectedOption == 2) {
            subOptionLabel.setText("Repeat every month on day:");
            subOptionSpinner.setAdapter(monthAdapter);
            subOptionSpinner.setVisibility(android.view.View.VISIBLE);
            yearlyDateButton.setVisibility(android.view.View.GONE);
            for (int i = 0; i < monthDays.length; i++) {
                if (monthDays[i].equals(state.selectedDay)) {
                    subOptionSpinner.setSelection(i);
                    break;
                }
            }
            subOptionLayout.setVisibility(android.view.View.VISIBLE);
        } else if (state.selectedOption == 3) {
            subOptionLabel.setText("Tap to choose which date each year:");
            subOptionSpinner.setVisibility(android.view.View.GONE);
            yearlyDateButton.setVisibility(android.view.View.VISIBLE);
            if (!state.selectedYearlyDate.isEmpty()) {
                try {
                    java.text.SimpleDateFormat parseFmt =
                            new java.text.SimpleDateFormat("MM-dd",
                                    java.util.Locale.getDefault());
                    java.text.SimpleDateFormat displayFmt =
                            new java.text.SimpleDateFormat("MMMM d",
                                    java.util.Locale.getDefault());
                    java.util.Date parsed = parseFmt.parse(state.selectedYearlyDate);
                    if (parsed != null) {
                        yearlyDateButton.setText(
                                "Selected: " + displayFmt.format(parsed));
                    }
                } catch (java.text.ParseException e) {
                    yearlyDateButton.setText("Tap to choose date");
                }
            } else {
                yearlyDateButton.setText("Tap to choose date");
            }
            subOptionLayout.setVisibility(android.view.View.VISIBLE);
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            state.selectedOption = checkedId;

            switch (checkedId) {
                case 1:
                    subOptionLabel.setText("Repeat every week on:");
                    subOptionSpinner.setAdapter(weekAdapter);
                    subOptionSpinner.setVisibility(android.view.View.VISIBLE);
                    yearlyDateButton.setVisibility(android.view.View.GONE);
                    if (!state.selectedDay.isEmpty()) {
                        for (int i = 0; i < weekDays.length; i++) {
                            if (weekDays[i].equals(state.selectedDay)) {
                                subOptionSpinner.setSelection(i);
                                break;
                            }
                        }
                    }
                    subOptionLayout.setVisibility(android.view.View.VISIBLE);
                    previewLabel.setText("Will be set to: Every " + state.selectedDay);
                    break;

                case 2:
                    subOptionLabel.setText("Repeat every month on day:");
                    subOptionSpinner.setAdapter(monthAdapter);
                    subOptionSpinner.setVisibility(android.view.View.VISIBLE);
                    yearlyDateButton.setVisibility(android.view.View.GONE);
                    if (!state.selectedDay.isEmpty()) {
                        for (int i = 0; i < monthDays.length; i++) {
                            if (monthDays[i].equals(state.selectedDay)) {
                                subOptionSpinner.setSelection(i);
                                break;
                            }
                        }
                    }
                    subOptionLayout.setVisibility(android.view.View.VISIBLE);
                    previewLabel.setText("Will be set to: Every month on day "
                            + state.selectedDay);
                    break;

                case 3:
                    subOptionLabel.setText("Tap to choose which date each year:");
                    subOptionSpinner.setVisibility(android.view.View.GONE);
                    yearlyDateButton.setVisibility(android.view.View.VISIBLE);
                    if (state.selectedYearlyDate.isEmpty()) {
                        yearlyDateButton.setText("Tap to choose date");
                        previewLabel.setText("Will be set to: Every year — tap button to pick date");
                    } else {
                        try {
                            java.text.SimpleDateFormat parseFmt =
                                    new java.text.SimpleDateFormat("MM-dd",
                                            java.util.Locale.getDefault());
                            java.text.SimpleDateFormat displayFmt =
                                    new java.text.SimpleDateFormat("MMMM d",
                                            java.util.Locale.getDefault());
                            java.util.Date parsed = parseFmt.parse(state.selectedYearlyDate);
                            if (parsed != null) {
                                String display = displayFmt.format(parsed);
                                yearlyDateButton.setText("Selected: " + display);
                                previewLabel.setText("Will be set to: Every year on " + display);
                            }
                        } catch (java.text.ParseException e) {
                            yearlyDateButton.setText("Tap to choose date");
                        }
                    }
                    subOptionLayout.setVisibility(android.view.View.VISIBLE);
                    break;

                default:
                    subOptionLayout.setVisibility(android.view.View.GONE);
                    state.selectedDay = "";
                    previewLabel.setText("Will be set to: None");
                    break;
            }
        });

        new AlertDialog.Builder(this)
                .setTitle("Set Recurrence")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {

                    switch (state.selectedOption) {

                        case 0:
                            selectedList.setRecurringType("none");
                            selectedList.setRecurringValue("");
                            refreshVisibleLists();
                            saveData(this);
                            Snackbar.make(recyclerView,
                                    "Recurrence removed",
                                    Snackbar.LENGTH_SHORT).show();
                            break;

                        case 1:
                            String day = state.selectedDay.isEmpty()
                                    ? "Sunday" : state.selectedDay;
                            selectedList.setRecurringType("weekly");
                            selectedList.setRecurringValue(day);
                            refreshVisibleLists();
                            saveData(this);
                            Snackbar.make(recyclerView,
                                    "Repeats every " + day,
                                    Snackbar.LENGTH_SHORT).show();
                            break;

                        case 2:
                            String dayNum = state.selectedDay.isEmpty()
                                    ? "1" : state.selectedDay;
                            selectedList.setRecurringType("monthly");
                            selectedList.setRecurringValue(dayNum);
                            refreshVisibleLists();
                            saveData(this);
                            Snackbar.make(recyclerView,
                                    "Repeats every month on day " + dayNum,
                                    Snackbar.LENGTH_SHORT).show();
                            break;

                        case 3:
                            if (state.selectedYearlyDate.isEmpty()) {
                                Snackbar.make(recyclerView,
                                        "Please tap the button to choose a date",
                                        Snackbar.LENGTH_SHORT).show();
                                return;
                            }
                            selectedList.setRecurringType("yearly");
                            selectedList.setRecurringValue(state.selectedYearlyDate);
                            refreshVisibleLists();
                            saveData(this);
                            try {
                                java.text.SimpleDateFormat parseFmt =
                                        new java.text.SimpleDateFormat("MM-dd",
                                                java.util.Locale.getDefault());
                                java.text.SimpleDateFormat displayFmt =
                                        new java.text.SimpleDateFormat("MMMM d",
                                                java.util.Locale.getDefault());
                                java.util.Date parsed =
                                        parseFmt.parse(state.selectedYearlyDate);
                                String display = parsed != null
                                        ? displayFmt.format(parsed)
                                        : state.selectedYearlyDate;
                                Snackbar.make(recyclerView,
                                        "Repeats every year on " + display,
                                        Snackbar.LENGTH_SHORT).show();
                            } catch (java.text.ParseException e) {
                                Snackbar.make(recyclerView,
                                        "Repeats every year",
                                        Snackbar.LENGTH_SHORT).show();
                            }
                            break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // --------------------------------------------------
// ARCHIVE LIST
// --------------------------------------------------
    private void archiveList(ShoppingList selectedList) {

        selectedList.setArchived(true);
        refreshVisibleLists();
        saveData(this);

        Snackbar.make(recyclerView, "List archived", Snackbar.LENGTH_LONG)
                .setAction("UNDO", v -> {
                    selectedList.setArchived(false);
                    refreshVisibleLists();
                    saveData(this);
                })
                .show();
    }

    // --------------------------------------------------
// DELETE LIST
// --------------------------------------------------
    private void deleteList(ShoppingList selectedList) {

        shoppingLists.remove(selectedList);
        refreshVisibleLists();
        saveData(this);

        Snackbar.make(recyclerView, "List deleted", Snackbar.LENGTH_LONG)
                .setAction("UNDO", v -> {
                    shoppingLists.add(selectedList);
                    refreshVisibleLists();
                    saveData(this);
                })
                .show();
    }

    // --------------------------------------------------
// RETURNS HUMAN-READABLE CURRENT RECURRENCE STATE
// --------------------------------------------------
    private String getCurrentRecurrenceDescription(ShoppingList list) {

        if (list.getRecurringType() == null) return "None";

        switch (list.getRecurringType()) {
            case "weekly":
                return "Every " + list.getRecurringValue();
            case "monthly":
                return "Every month on day " + list.getRecurringValue();
            case "yearly":
                return "Every year on " + list.getRecurringValue();
            default:
                return "None";
        }
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.menu_archived) {
            startActivity(new Intent(this, ArchivedListsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshVisibleLists();
    }

    // --------------------------------------------------
// CHECK RECURRING LISTS
// --------------------------------------------------
    private void checkRecurringLists() {

        java.util.Calendar calendar = java.util.Calendar.getInstance();

        String todayDay =
                new java.text.SimpleDateFormat("EEEE",
                        java.util.Locale.getDefault()).format(calendar.getTime());

        int dayOfMonth = calendar.get(java.util.Calendar.DAY_OF_MONTH);

        String todayMonthDay =
                new java.text.SimpleDateFormat("MM-dd",
                        java.util.Locale.getDefault()).format(calendar.getTime());

        String todayDate =
                new java.text.SimpleDateFormat("yyyy-MM-dd",
                        java.util.Locale.getDefault()).format(calendar.getTime());

        for (ShoppingList list : shoppingLists) {

            // -----------------------------------------------
            // LIST-LEVEL RECURRENCE (existing logic)
            // -----------------------------------------------
            if (list.isArchived()) {
                if (!todayDate.equals(list.getLastRestoredDate())) {

                    if (list.getRecurringType() == null) {
                        list.setRecurringType("none");
                    }

                    switch (list.getRecurringType()) {
                        case "weekly":
                            if (todayDay.equalsIgnoreCase(list.getRecurringValue())) {
                                list.setArchived(false);
                                list.resetItems();
                                list.setLastRestoredDate(todayDate);
                            }
                            break;
                        case "monthly":
                            try {
                                if (dayOfMonth == Integer.parseInt(list.getRecurringValue())) {
                                    list.setArchived(false);
                                    list.resetItems();
                                    list.setLastRestoredDate(todayDate);
                                }
                            } catch (NumberFormatException e) {
                                // malformed value — skip
                            }
                            break;
                        case "yearly":
                            if (todayMonthDay.equals(list.getRecurringValue())) {
                                list.setArchived(false);
                                list.resetItems();
                                list.setLastRestoredDate(todayDate);
                            }
                            break;
                    }
                }
            }

            // -----------------------------------------------
            // ITEM-LEVEL RECURRENCE (new logic)
            // Only runs on non-archived lists
            // -----------------------------------------------
            if (!list.isArchived()) {
                for (ListSection section : list.getSections()) {
                    for (ShoppingItem item : section.getItems()) {

                        if (!item.hasRecurrence()) continue;
                        if (todayDate.equals(item.getLastRestoredDate())) continue;

                        switch (item.getRecurringType()) {
                            case "weekly":
                                if (todayDay.equalsIgnoreCase(item.getRecurringValue())) {
                                    item.setChecked(false);
                                    item.setLastRestoredDate(todayDate);
                                }
                                break;
                            case "monthly":
                                try {
                                    if (dayOfMonth == Integer.parseInt(item.getRecurringValue())) {
                                        item.setChecked(false);
                                        item.setLastRestoredDate(todayDate);
                                    }
                                } catch (NumberFormatException e) {
                                    // malformed value — skip
                                }
                                break;
                            case "yearly":
                                if (todayMonthDay.equals(item.getRecurringValue())) {
                                    item.setChecked(false);
                                    item.setLastRestoredDate(todayDate);
                                }
                                break;
                        }
                    }
                }
            }
        }

        refreshVisibleLists();
    }

    // --------------------------------------------------
// LOAD LISTS FROM SHARED PREFERENCES
// --------------------------------------------------
    private void loadData() {

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String json = prefs.getString(LIST_KEY, null);

        if (json != null) {
            Type type = new TypeToken<List<ShoppingList>>() {}.getType();
            List<ShoppingList> savedLists = gson.fromJson(json, type);
            shoppingLists.clear();
            shoppingLists.addAll(savedLists);

            for (ShoppingList list : shoppingLists) {
                if (list.getSections() == null || list.getSections().isEmpty()) {
                    list.getSections().add(new ListSection(""));
                }
            }
        }
    }

    // --------------------------------------------------
// SAVE LISTS TO SHARED PREFERENCES
// --------------------------------------------------
    public static void saveData(Context context) {

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(LIST_KEY, gson.toJson(shoppingLists));
        editor.apply();
    }

    // --------------------------------------------------
// FILTER VISIBLE LISTS
// --------------------------------------------------
    private void refreshVisibleLists() {

        visibleLists.clear();

        for (ShoppingList list : shoppingLists) {
            if (!list.isArchived() && list.isFavorite()) {
                visibleLists.add(list);
            }
        }

        for (ShoppingList list : shoppingLists) {
            if (!list.isArchived() && !list.isFavorite()) {
                visibleLists.add(list);
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}