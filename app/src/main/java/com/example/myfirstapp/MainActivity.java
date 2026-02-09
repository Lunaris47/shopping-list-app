package com.example.myfirstapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final List<ShoppingList> shoppingLists = new ArrayList<>();
    private ListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Views (keep for now, we’ll reuse later)
        EditText inputText = findViewById(R.id.inputText);
        Button addButton = findViewById(R.id.addButton);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        // Sample lists to display as cards
        shoppingLists.add(new ShoppingList("Groceries"));
        shoppingLists.add(new ShoppingList("Homework"));
        shoppingLists.add(new ShoppingList("Chores"));
        shoppingLists.add(new ShoppingList("Packing"));

        // Setup RecyclerView as a grid (2 columns like Google Keep)
        adapter = new ListAdapter(shoppingLists);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);

        // Temporarily disable old add-item behavior
        addButton.setEnabled(false);
        inputText.setEnabled(false);
    }
}
