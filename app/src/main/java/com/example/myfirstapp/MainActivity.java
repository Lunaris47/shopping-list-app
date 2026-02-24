package com.example.myfirstapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final List<ShoppingList> shoppingLists = new ArrayList<>();
    private ListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        FloatingActionButton fab = findViewById(R.id.fabAddList);

        // Add sample lists only once
        if (shoppingLists.isEmpty()) {
            shoppingLists.add(new ShoppingList("Groceries"));
            shoppingLists.add(new ShoppingList("Homework"));
            shoppingLists.add(new ShoppingList("Chores"));
            shoppingLists.add(new ShoppingList("Packing"));
        }

        adapter = new ListAdapter(shoppingLists, position -> {

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Delete List")
                    .setMessage("Are you sure you want to delete this list?")
                    .setPositiveButton("Delete", (dialog, which) -> {

                        ShoppingList deletedList = shoppingLists.get(position);
                        shoppingLists.remove(position);
                        adapter.notifyItemRemoved(position);

                        Snackbar.make(recyclerView, "List deleted", Snackbar.LENGTH_LONG)
                                .setAction("UNDO", v -> {
                                    shoppingLists.add(position, deletedList);
                                    adapter.notifyItemInserted(position);
                                })
                                .show();

                    })
                    .setNegativeButton("Cancel", null)
                    .show();

        });

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);

// FAB click → create new list
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
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }
}