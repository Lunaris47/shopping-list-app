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

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
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

            String[] options = {"Archive List", "Delete List"};

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("List Options")
                    .setItems(options, (dialog, which) -> {

                        // Get the list selected from visible lists
                        ShoppingList selectedList = visibleLists.get(position);

                        // -------------------------
// ARCHIVE LIST
// -------------------------
                        if (which == 0) {

                            String[] repeatOptions = {"No", "Weekly", "Monthly", "Yearly"};

                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Repeat this list?")
                                    .setItems(repeatOptions, (dialog2, repeatChoice) -> {

                                        // -------------------------
                                        // NO REPEAT
                                        // -------------------------
                                        if (repeatChoice == 0) {

                                            selectedList.setRecurringType("none");
                                            selectedList.setRecurringValue("");

                                            selectedList.setArchived(true);

                                            refreshVisibleLists();
                                            saveData(this);

                                            Snackbar.make(recyclerView, "List archived",
                                                            Snackbar.LENGTH_LONG)
                                                    .setAction("UNDO", v -> {

                                                        selectedList.setArchived(false);
                                                        selectedList.setRecurringType("none");

                                                        refreshVisibleLists();
                                                        saveData(this);

                                                    })
                                                    .show();
                                        }

                                        // -------------------------
                                        // WEEKLY
                                        // -------------------------
                                        else if (repeatChoice == 1) {

                                            String[] days = {
                                                    "Sunday","Monday","Tuesday",
                                                    "Wednesday","Thursday","Friday","Saturday"
                                            };

                                            new AlertDialog.Builder(MainActivity.this)
                                                    .setTitle("Repeat every week on:")
                                                    .setItems(days, (dialog3, dayIndex) -> {

                                                        selectedList.setRecurringType("weekly");
                                                        selectedList.setRecurringValue(days[dayIndex]);

                                                        selectedList.setArchived(true);

                                                        refreshVisibleLists();
                                                        saveData(this);

                                                        Snackbar.make(recyclerView,
                                                                "List archived (weekly)",
                                                                Snackbar.LENGTH_LONG).show();
                                                    })
                                                    .show();
                                        }

                                        // -------------------------
                                        // MONTHLY
                                        // -------------------------
                                        else if (repeatChoice == 2) {

                                            String[] days = new String[31];

                                            for (int i = 0; i < 31; i++) {
                                                days[i] = String.valueOf(i + 1);
                                            }

                                            new AlertDialog.Builder(MainActivity.this)
                                                    .setTitle("Repeat every month on day:")
                                                    .setItems(days, (dialog3, dayIndex) -> {

                                                        selectedList.setRecurringType("monthly");
                                                        selectedList.setRecurringValue(days[dayIndex]);

                                                        selectedList.setArchived(true);

                                                        refreshVisibleLists();
                                                        saveData(this);

                                                        Snackbar.make(recyclerView,
                                                                "List archived (monthly)",
                                                                Snackbar.LENGTH_LONG).show();
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

                                            selectedList.setArchived(true);

                                            refreshVisibleLists();
                                            saveData(this);

                                            Snackbar.make(recyclerView,
                                                    "List archived (yearly)",
                                                    Snackbar.LENGTH_LONG).show();
                                        }

                                    })
                                    .show();
                        }

                        // -------------------------
                        // DELETE LIST
                        // -------------------------
                        else if (which == 1) {

                            shoppingLists.remove(selectedList);

                            refreshVisibleLists();
                            saveData(this);

                            Snackbar.make(recyclerView,
                                            "List deleted",
                                            Snackbar.LENGTH_LONG)
                                    .setAction("UNDO", v -> {

                                        shoppingLists.add(selectedList);
                                        refreshVisibleLists();
                                        saveData(this);

                                    })
                                    .show();
                        }

                    })
                    .setNegativeButton("Cancel", null)
                    .show();

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

                    if (dayOfMonth ==
                            Integer.parseInt(list.getRecurringValue())) {

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

        String json = gson.toJson(shoppingLists);

        editor.putString(LIST_KEY, json);
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