package com.example.myfirstapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // 🔹 Master list of ALL lists (including archived ones)
    public static final List<ShoppingList> shoppingLists = new ArrayList<>();

    // 🔹 Lists currently visible on the home screen
    private final List<ShoppingList> visibleLists = new ArrayList<>();

    private ListAdapter adapter;

    // 🔹 Promoted to instance field so all methods can reference it
    private RecyclerView recyclerView;

    // 🔹 SharedPreferences keys
    private static final String PREFS_NAME = "shopping_prefs";
    private static final String LIST_KEY = "shopping_lists";

    // 🔹 Gson instance used for saving/loading JSON
    private static final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));

        recyclerView = findViewById(R.id.recyclerView);
        FloatingActionButton fab = findViewById(R.id.fabAddList);

        // 🔹 Load saved lists from SharedPreferences
        loadData();

        // 🔹 Restore recurring lists if today matches their scheduled day
        checkRecurringLists();

        // 🔹 Filter visible lists (exclude archived)
        refreshVisibleLists();

        // 🔹 Add default sample lists only if no saved data exists
        if (shoppingLists.isEmpty()) {
            shoppingLists.add(new ShoppingList("Groceries"));
            shoppingLists.add(new ShoppingList("Homework"));
            shoppingLists.add(new ShoppingList("Chores"));
            shoppingLists.add(new ShoppingList("Packing"));
            saveData(this);
        }

        // 🔹 Adapter for displaying visible lists
        adapter = new ListAdapter(visibleLists, position -> {
            ShoppingList selectedList = visibleLists.get(position);
            showListOptions(selectedList);
        });

        // 🔹 Grid layout (2 list cards per row)
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);

        // --------------------------------------------------
        // FAB → CREATE NEW LIST
        // --------------------------------------------------
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
    // Three clean, separate options
    // --------------------------------------------------
    private void showListOptions(ShoppingList selectedList) {

        String[] options = {"Set Recurrence", "Archive List", "Delete List"};

        new AlertDialog.Builder(this)
                .setTitle("List Options")
                .setItems(options, (dialog, which) -> {

                    if (which == 0) {
                        // -------------------------
                        // SET RECURRENCE
                        // -------------------------
                        showRecurrenceDialog(selectedList);

                    } else if (which == 1) {
                        // -------------------------
                        // ARCHIVE LIST
                        // -------------------------
                        archiveList(selectedList);

                    } else if (which == 2) {
                        // -------------------------
                        // DELETE LIST
                        // -------------------------
                        deleteList(selectedList);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // --------------------------------------------------
// SET RECURRENCE
// Completely independent from archiving
// Can be set, changed, or removed at any time
// --------------------------------------------------
    private void showRecurrenceDialog(ShoppingList selectedList) {

        // Pre-declare arrays outside of lambda scope to avoid compiler issues
        final String[] weekDays = {
                "Sunday", "Monday", "Tuesday",
                "Wednesday", "Thursday", "Friday", "Saturday"
        };

        // Build monthDays using Array
        final String[] monthDays = {
                "1","2","3","4","5","6","7","8","9","10",
                "11","12","13","14","15","16","17","18","19","20",
                "21","22","23","24","25","26","27","28","29","30",
                "31"
        };

        // Show the current recurrence state as the dialog message
        // so the user always knows what is currently set
        String currentSetting = getCurrentRecurrenceDescription(selectedList);

        new AlertDialog.Builder(this)
                .setTitle("Set Recurrence — Current: " + currentSetting)
                .setItems(new String[]{"No Recurrence", "Weekly", "Monthly", "Yearly"},

                        (dialog, repeatChoice) -> {

                            // -------------------------
                            // NO RECURRENCE
                            // -------------------------
                            if (repeatChoice == 0) {

                                selectedList.setRecurringType("none");
                                selectedList.setRecurringValue("");

                                refreshVisibleLists();
                                saveData(this);

                                Snackbar.make(recyclerView,
                                        "Recurrence removed",
                                        Snackbar.LENGTH_SHORT).show();
                            }

                            // -------------------------
                            // WEEKLY
                            // -------------------------
                            else if (repeatChoice == 1) {

                                new AlertDialog.Builder(this)
                                        .setTitle("Repeat every week on:")
                                        .setItems(weekDays, (dialog2, dayIndex) -> {

                                            selectedList.setRecurringType("weekly");
                                            selectedList.setRecurringValue(weekDays[dayIndex]);

                                            refreshVisibleLists();
                                            saveData(this);

                                            Snackbar.make(recyclerView,
                                                    "Repeats every " + weekDays[dayIndex],
                                                    Snackbar.LENGTH_SHORT).show();
                                        })
                                        .show();
                            }

                            // -------------------------
                            // MONTHLY
                            // -------------------------
                            else if (repeatChoice == 2) {

                                new AlertDialog.Builder(this)
                                        .setTitle("Repeat every month on day:")
                                        .setItems(monthDays, (dialog2, dayIndex) -> {

                                            selectedList.setRecurringType("monthly");
                                            selectedList.setRecurringValue(monthDays[dayIndex]);

                                            refreshVisibleLists();
                                            saveData(this);

                                            Snackbar.make(recyclerView,
                                                    "Repeats every month on day " + monthDays[dayIndex],
                                                    Snackbar.LENGTH_SHORT).show();
                                        })
                                        .show();
                            }

                            // -------------------------
                            // YEARLY
                            // -------------------------
                            else if (repeatChoice == 3) {

                                java.text.SimpleDateFormat sdf =
                                        new java.text.SimpleDateFormat("MM-dd",
                                                java.util.Locale.getDefault());

                                String today = sdf.format(new java.util.Date());

                                selectedList.setRecurringType("yearly");
                                selectedList.setRecurringValue(today);

                                refreshVisibleLists();
                                saveData(this);

                                Snackbar.make(recyclerView,
                                        "Repeats every year on this date",
                                        Snackbar.LENGTH_SHORT).show();
                            }
                        })
                .show();
    }



    // --------------------------------------------------
    // ARCHIVE LIST
    // Clean single action — no follow-up dialogs
    // Recurrence settings are preserved as-is
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
    // Removes permanently with undo
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
    // Used as the subtitle in the recurrence dialog
    // so users always know what is currently set
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

        // Reload lists when returning from ArchivedListsActivity
        refreshVisibleLists();
    }

    // --------------------------------------------------
    // CHECK RECURRING LISTS
    // Automatically restore archived recurring lists
    // when their scheduled day arrives
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

            if (!list.isArchived()) continue;
            if (todayDate.equals(list.getLastRestoredDate())) continue;

            switch (list.getRecurringType()) {

                case "weekly":
                    if (todayDay.equalsIgnoreCase(list.getRecurringValue())) {
                        list.setArchived(false);
                        list.resetItems();
                        list.setLastRestoredDate(todayDate);
                    }
                    break;

                case "monthly":
                    if (dayOfMonth == Integer.parseInt(list.getRecurringValue())) {
                        list.setArchived(false);
                        list.resetItems();
                        list.setLastRestoredDate(todayDate);
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
    // Only show non-archived lists on the home screen
    // --------------------------------------------------
    private void refreshVisibleLists() {

        visibleLists.clear();

        for (ShoppingList list : shoppingLists) {
            if (!list.isArchived()) {
                visibleLists.add(list);
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}