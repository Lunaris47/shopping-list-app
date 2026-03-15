package com.example.myfirstapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;

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

    public static final List<ShoppingList> shoppingLists = new ArrayList<>();
    private final List<ShoppingList> visibleLists = new ArrayList<>();
    private ListAdapter adapter;

    private static final String PREFS_NAME = "shopping_prefs";
    private static final String LIST_KEY = "shopping_lists";
    private static final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        FloatingActionButton fab = findViewById(R.id.fabAddList);

        // 🔹 Load saved data first
        loadData();
        checkRecurringLists();
        refreshVisibleLists();

        // 🔹 Add sample lists ONLY if nothing saved yet
        if (shoppingLists.isEmpty()) {
            shoppingLists.add(new ShoppingList("Groceries"));
            shoppingLists.add(new ShoppingList("Homework"));
            shoppingLists.add(new ShoppingList("Chores"));
            shoppingLists.add(new ShoppingList("Packing"));
            saveData(this);
        }

        adapter = new ListAdapter(visibleLists, position -> {

            String[] options = {"Archive List", "Delete List"};

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("List Options")
                    .setItems(options, (dialog, which) -> {

                        if (which == 0) { // Archive

                            shoppingLists.get(position).setArchived(true);
                            refreshVisibleLists();
                            saveData(this);

                            Snackbar.make(recyclerView,
                                            "List archived",
                                            Snackbar.LENGTH_LONG)
                                    .show();

                        } else if (which == 1) { // Delete

                            ShoppingList deletedList = shoppingLists.get(position);

                            shoppingLists.remove(position);
                            refreshVisibleLists();
                            saveData(this);

                            Snackbar.make(recyclerView,
                                            "List deleted",
                                            Snackbar.LENGTH_LONG)
                                    .setAction("UNDO", v -> {
                                        shoppingLists.add(position, deletedList);
                                        refreshVisibleLists();
                                        saveData(this);
                                    })
                                    .show();
                        }

                    })
                    .setNegativeButton("Cancel", null)
                    .show();

        });

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);

        // 🔹 FAB → create new list
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
                            refreshVisibleLists();;
                            saveData(this);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

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
            }
        }
    }

    // 🔹 Load data from SharedPreferences
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

    // 🔹 Save data to SharedPreferences
    public static void saveData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String json = gson.toJson(shoppingLists);
        editor.putString(LIST_KEY, json);
        editor.apply();
    }

    // Keeps adapter synced with only ACTIVE lists.
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