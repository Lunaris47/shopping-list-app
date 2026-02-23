package com.example.myfirstapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ListDetailActivity extends AppCompatActivity {

    private ShoppingList shoppingList;
    private ItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_details);

        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        int index = getIntent().getIntExtra("list_index", -1);
        shoppingList = MainActivity.shoppingLists.get(index);

        EditText input = findViewById(R.id.itemInput);
        Button addButton = findViewById(R.id.addItemButton);
        RecyclerView recyclerView = findViewById(R.id.detailRecyclerView);

        adapter = new ItemAdapter(shoppingList.getItems());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Add item logic
        addButton.setOnClickListener(v -> {
            String item = input.getText().toString().trim();
            if (!item.isEmpty()) {
                shoppingList.addItem(item);
                adapter.notifyItemInserted(shoppingList.getItems().size() - 1);
                input.setText("");
            }
        });

        // ⭐ Swipe to delete setup
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(
                        0,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT
                ) {

                    @Override
                    public boolean onMove(RecyclerView recyclerView,
                                          RecyclerView.ViewHolder viewHolder,
                                          RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder,
                                         int direction) {

                        int position = viewHolder.getAdapterPosition();

                        shoppingList.getItems().remove(position);
                        adapter.notifyItemRemoved(position);

                        Toast.makeText(
                                ListDetailActivity.this,
                                "Item deleted",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );

        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}