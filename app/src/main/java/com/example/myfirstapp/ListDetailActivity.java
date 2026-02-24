package com.example.myfirstapp;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

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

        // Add item
        addButton.setOnClickListener(v -> {
            String item = input.getText().toString().trim();
            if (!item.isEmpty()) {
                shoppingList.addItem(item);
                adapter.notifyItemInserted(shoppingList.getItems().size() - 1);
                input.setText("");
            }
        });

        // Swipe helper
        ItemTouchHelper helper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(0,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

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
                        String deletedItem = shoppingList.getItems().get(position);

                        shoppingList.getItems().remove(position);
                        adapter.notifyItemRemoved(position);

                        Snackbar.make(recyclerView,
                                        "Item deleted",
                                        Snackbar.LENGTH_LONG)
                                .setAction("UNDO", v -> {
                                    shoppingList.getItems().add(position, deletedItem);
                                    adapter.notifyItemInserted(position);
                                })
                                .show();
                    }

                    @Override
                    public void onChildDraw(Canvas c,
                                            RecyclerView recyclerView,
                                            RecyclerView.ViewHolder viewHolder,
                                            float dX,
                                            float dY,
                                            int actionState,
                                            boolean isCurrentlyActive) {

                        View itemView = viewHolder.itemView;

                        Paint paint = new Paint();
                        paint.setColor(Color.RED);

                        if (dX > 0) {
                            c.drawRect(itemView.getLeft(),
                                    itemView.getTop(),
                                    itemView.getLeft() + dX,
                                    itemView.getBottom(),
                                    paint);
                        } else {
                            c.drawRect(itemView.getRight() + dX,
                                    itemView.getTop(),
                                    itemView.getRight(),
                                    itemView.getBottom(),
                                    paint);
                        }

                        Drawable icon = getResources().getDrawable(R.drawable.ic_delete);

                        int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                        int iconTop = itemView.getTop() + iconMargin;
                        int iconBottom = iconTop + icon.getIntrinsicHeight();

                        if (dX > 0) {
                            int iconLeft = itemView.getLeft() + iconMargin;
                            int iconRight = iconLeft + icon.getIntrinsicWidth();
                            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                        } else {
                            int iconRight = itemView.getRight() - iconMargin;
                            int iconLeft = iconRight - icon.getIntrinsicWidth();
                            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                        }

                        icon.draw(c);

                        super.onChildDraw(c, recyclerView, viewHolder,
                                dX, dY, actionState, isCurrentlyActive);
                    }
                });

        helper.attachToRecyclerView(recyclerView);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}