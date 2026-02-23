package com.example.myfirstapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ListDetailActivity extends AppCompatActivity {

    private ShoppingList shoppingList;
    private ItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_details);

        int index = getIntent().getIntExtra("list_index", -1);
        shoppingList = MainActivity.shoppingLists.get(index);

        EditText input = findViewById(R.id.itemInput);
        Button addButton = findViewById(R.id.addItemButton);
        RecyclerView recyclerView = findViewById(R.id.detailRecyclerView);

        adapter = new ItemAdapter(shoppingList.getItems());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        addButton.setOnClickListener(v -> {
            String item = input.getText().toString().trim();
            if (!item.isEmpty()) {
                shoppingList.addItem(item);
                adapter.notifyItemInserted(shoppingList.getItems().size() - 1);
                input.setText("");
            }
        });
    }
}