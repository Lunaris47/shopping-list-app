package com.example.myfirstapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ArchivedListsActivity extends AppCompatActivity {

    private final List<ShoppingList> archivedLists = new ArrayList<>();
    private ArchivedListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archived_lists);

        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewArchived);

        loadArchivedLists();

        adapter = new ArchivedListAdapter(archivedLists, list -> {

            list.setArchived(false);

            MainActivity.saveData(this);
            loadArchivedLists();
            adapter.notifyDataSetChanged();
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadArchivedLists() {

        archivedLists.clear();

        for (ShoppingList list : MainActivity.shoppingLists) {
            if (list.isArchived()) {
                archivedLists.add(list);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}