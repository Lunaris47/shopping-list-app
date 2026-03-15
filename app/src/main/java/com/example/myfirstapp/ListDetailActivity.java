package com.example.myfirstapp;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

public class ListDetailActivity extends AppCompatActivity {

    private ShoppingList shoppingList;
    private ItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        boolean archivedView =
                getIntent().getBooleanExtra("archived_view", false);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_details);

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        int index = getIntent().getIntExtra("list_index", -1);
        shoppingList = MainActivity.shoppingLists.get(index);

        EditText input = findViewById(R.id.itemInput);
        Button addButton = findViewById(R.id.addItemButton);
        RecyclerView recyclerView = findViewById(R.id.detailRecyclerView);

        input.requestFocus();

        input.post(() -> {
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager)
                            getSystemService(INPUT_METHOD_SERVICE);

            if (imm != null) {
                imm.showSoftInput(input,
                        android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }
        });

        input.setOnEditorActionListener((v, actionId, event) -> {
            String text = input.getText().toString().trim();

            if (!text.isEmpty()) {
                shoppingList.addItem(text);
                adapter.notifyItemInserted(shoppingList.getItems().size() - 1);
                input.setText("");
                MainActivity.saveData(ListDetailActivity.this);
            }

            return true;
        });

        // Adapter with toggle callback
        adapter = new ItemAdapter(
                shoppingList.getItems(),
                () -> MainActivity.saveData(ListDetailActivity.this)
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(new androidx.recyclerview.widget.DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        recyclerView.setEdgeEffectFactory(new RecyclerView.EdgeEffectFactory());

        // Add item
        if (!archivedView) {

            addButton.setOnClickListener(v -> {
                String text = input.getText().toString().trim();
                if (!text.isEmpty()) {
                    shoppingList.addItem(text);
                    adapter.notifyItemInserted(shoppingList.getItems().size() - 1);
                    input.setText("");
                    input.requestFocus();
                    MainActivity.saveData(ListDetailActivity.this);
                }
            });

        } else {

            // Disable editing for archived lists
            input.setEnabled(false);
            addButton.setEnabled(false);
        }

        // Swipe to delete
        ItemTouchHelper helper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(
                        0,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT
                ) {

                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder,
                                         int direction) {

                        int position = viewHolder.getBindingAdapterPosition();
                        if (position == RecyclerView.NO_POSITION) return;

                        ShoppingItem deletedItem =
                                shoppingList.getItems().get(position);

                        shoppingList.getItems().remove(position);
                        adapter.notifyItemRemoved(position);
                        MainActivity.saveData(ListDetailActivity.this);

                        Snackbar.make(recyclerView,
                                        "Item deleted",
                                        Snackbar.LENGTH_LONG)
                                .setAction("UNDO", v -> {
                                    shoppingList.getItems()
                                            .add(position, deletedItem);
                                    adapter.notifyItemInserted(position);
                                    MainActivity.saveData(ListDetailActivity.this);
                                })
                                .show();
                    }

                    @Override
                    public void onChildDraw(@NonNull Canvas c,
                                            @NonNull RecyclerView recyclerView,
                                            @NonNull RecyclerView.ViewHolder viewHolder,
                                            float dX,
                                            float dY,
                                            int actionState,
                                            boolean isCurrentlyActive) {

                        View itemView = viewHolder.itemView;

                        Paint paint = new Paint();
                        paint.setColor(Color.RED);

                        if (dX > 0) {
                            c.drawRect(
                                    itemView.getLeft(),
                                    itemView.getTop(),
                                    itemView.getLeft() + dX,
                                    itemView.getBottom(),
                                    paint
                            );
                        } else {
                            c.drawRect(
                                    itemView.getRight() + dX,
                                    itemView.getTop(),
                                    itemView.getRight(),
                                    itemView.getBottom(),
                                    paint
                            );
                        }

                        Drawable icon = ResourcesCompat.getDrawable(
                                getResources(),
                                R.drawable.ic_delete,
                                null
                        );

                        if (icon != null) {
                            int iconMargin =
                                    (itemView.getHeight()
                                            - icon.getIntrinsicHeight()) / 2;

                            int iconTop = itemView.getTop() + iconMargin;
                            int iconBottom =
                                    iconTop + icon.getIntrinsicHeight();

                            if (dX > 0) {
                                int iconLeft =
                                        itemView.getLeft() + iconMargin;
                                int iconRight =
                                        iconLeft + icon.getIntrinsicWidth();
                                icon.setBounds(iconLeft,
                                        iconTop,
                                        iconRight,
                                        iconBottom);
                            } else {
                                int iconRight =
                                        itemView.getRight() - iconMargin;
                                int iconLeft =
                                        iconRight - icon.getIntrinsicWidth();
                                icon.setBounds(iconLeft,
                                        iconTop,
                                        iconRight,
                                        iconBottom);
                            }

                            icon.draw(c);
                        }

                        super.onChildDraw(
                                c,
                                recyclerView,
                                viewHolder,
                                dX,
                                dY,
                                actionState,
                                isCurrentlyActive
                        );
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