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

        // 🔹 Add sample lists ONLY if nothing saved yet
        if (shoppingLists.isEmpty()) {
            shoppingLists.add(new ShoppingList("Groceries"));
            shoppingLists.add(new ShoppingList("Homework"));
            shoppingLists.add(new ShoppingList("Chores"));
            shoppingLists.add(new ShoppingList("Packing"));
            saveData(this);
        }

        adapter = new ListAdapter(shoppingLists, position -> {

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Delete List")
                    .setMessage("Are you sure you want to delete this list?")
                    .setPositiveButton("Delete", (dialog, which) -> {

                        ShoppingList deletedList = shoppingLists.get(position);
                        shoppingLists.remove(position);
                        adapter.notifyItemRemoved(position);
                        saveData(this);

                        Snackbar.make(recyclerView, "List deleted", Snackbar.LENGTH_LONG)
                                .setAction("UNDO", v -> {
                                    shoppingLists.add(position, deletedList);
                                    adapter.notifyItemInserted(position);
                                    saveData(this);
                                })
                                .show();

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
                            adapter.notifyItemInserted(shoppingLists.size() - 1);
                            saveData(this);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
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
}