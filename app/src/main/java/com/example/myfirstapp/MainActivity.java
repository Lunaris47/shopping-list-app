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

                            selectedList.setArchived(true);

                            refreshVisibleLists();
                            saveData(this);

                            Snackbar.make(recyclerView,
                                            "List archived",
                                            Snackbar.LENGTH_LONG)
                                    .setAction("UNDO", v -> {

                                        selectedList.setArchived(false);
                                        refreshVisibleLists();
                                        saveData(this);

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

            new AlertDialog.Builder(this)
                    .setTitle("Create New List")
                    .setView(input)
                    .setPositiveButton("Create", (dialog, which) -> {

                        String name = input.getText().toString().trim();

                        if (!name.isEmpty()) {

                            shoppingLists.add(new ShoppingList(name));

                            refreshVisibleLists();
                            saveData(this);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // --------------------------------------------------
        // OPEN ARCHIVED LISTS SCREEN
        // Long press toolbar to view archived lists
        // --------------------------------------------------
        findViewById(R.id.toolbar).setOnLongClickListener(v -> {

            startActivity(new Intent(this, ArchivedListsActivity.class));

            return true;
        });
    }

    // --------------------------------------------------
    // CHECK RECURRING LISTS
    // Automatically restore archived recurring lists
    // when their scheduled day arrives
    // --------------------------------------------------
    private void checkRecurringLists() {

        String todayDay =
                new java.text.SimpleDateFormat("EEEE",
                        java.util.Locale.getDefault()).format(new java.util.Date());

        String todayDate =
                new java.text.SimpleDateFormat("yyyy-MM-dd",
                        java.util.Locale.getDefault()).format(new java.util.Date());

        for (ShoppingList list : shoppingLists) {

            if (list.isRecurring()
                    && list.isArchived()
                    && todayDay.equalsIgnoreCase(list.getRecurringDay())
                    && !todayDate.equals(list.getLastRestoredDate())) {

                list.setArchived(false);
                list.resetItems();
                list.setLastRestoredDate(todayDate);

                refreshVisibleLists();
                saveData(this); // ensure recurring restoration persists
            }
        }
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