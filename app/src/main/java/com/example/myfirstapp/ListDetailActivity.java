package com.example.myfirstapp;

import android.app.AlertDialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class ListDetailActivity extends AppCompatActivity {

    private ShoppingList shoppingList;
    private ItemAdapter adapter;
    private RecyclerView recyclerView;

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

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(shoppingList.getTitle());
        }

        recyclerView = findViewById(R.id.detailRecyclerView);
        FloatingActionButton fabAddSection = findViewById(R.id.fabAddSection);

        // -----------------------------------------------
        // ADAPTER
        // -----------------------------------------------
        adapter = new ItemAdapter(
                shoppingList.getSections(),
                shoppingList.getTitle(),
                archivedView,
                () -> MainActivity.saveData(ListDetailActivity.this),
                (deletedSection, sectionIndex) -> {
                    shoppingList.getSections().remove(deletedSection);

                    // -----------------------------------------------
                    // If no sections remain at all, restore the
                    // default section so the list is still usable
                    // -----------------------------------------------
                    boolean noSectionsLeft = shoppingList.getSections().isEmpty();
                    boolean hasNamedSections = shoppingList.getSections()
                            .stream()
                            .anyMatch(sec -> !sec.isDefaultSection());

                    if (noSectionsLeft || !hasNamedSections) {
                        boolean hasDefaultSection = shoppingList.getSections()
                                .stream()
                                .anyMatch(ListSection::isDefaultSection);
                        if (!hasDefaultSection) {
                            shoppingList.getSections().add(new ListSection(""));
                        }
                    }

                    adapter.refreshList();
                    MainActivity.saveData(ListDetailActivity.this);

                    Snackbar.make(recyclerView,
                                    "Section deleted",
                                    Snackbar.LENGTH_LONG)
                            .setAction("UNDO", v -> {

                                // On undo, remove the restored default
                                // section if we added one, then re-insert
                                // the deleted section at its original index
                                shoppingList.getSections().removeIf(
                                        ListSection::isDefaultSection);
                                shoppingList.getSections().add(
                                        sectionIndex, deletedSection);
                                adapter.refreshList();
                                MainActivity.saveData(ListDetailActivity.this);
                            })
                            .show();
                }
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(
                new androidx.recyclerview.widget.DefaultItemAnimator());
        recyclerView.setHasFixedSize(false);
        recyclerView.setEdgeEffectFactory(new RecyclerView.EdgeEffectFactory());

        // -----------------------------------------------
        // FAB — ADD SECTION
        // Hidden in archived view
        // -----------------------------------------------
        if (!archivedView) {
            fabAddSection.setOnClickListener(v -> {
                EditText sectionInput = new EditText(this);
                sectionInput.setHint("Section name");
                sectionInput.setSingleLine(true);

                new AlertDialog.Builder(this)
                        .setTitle("New Section")
                        .setView(sectionInput)
                        .setPositiveButton("Add", (dialog, which) -> {
                            String name = sectionInput.getText()
                                    .toString().trim();
                            if (!name.isEmpty()) {

                                // -----------------------------------------------
                                // Check if this is the first named section
                                // being added to the list
                                // -----------------------------------------------
                                boolean hasNamedSections = shoppingList.getSections()
                                        .stream()
                                        .anyMatch(sec -> !sec.isDefaultSection());

                                if (!hasNamedSections) {
                                    // This is the first named section being added
                                    ListSection defaultSection = shoppingList.getSections()
                                            .stream()
                                            .filter(ListSection::isDefaultSection)
                                            .findFirst()
                                            .orElse(null);

                                    if (defaultSection != null
                                            && defaultSection.getItems().isEmpty()) {
                                        // Default section is empty — remove it
                                        // so "Other" never appears
                                        shoppingList.getSections().remove(defaultSection);
                                    }
                                    // If default section has items, leave it —
                                    // buildFlatList() will render it as "Other" at the bottom
                                }

                                shoppingList.addSection(name);
                                adapter.refreshList();
                                MainActivity.saveData(ListDetailActivity.this);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        } else {
            fabAddSection.setVisibility(View.GONE);
        }

        // -----------------------------------------------
        // ITEM TOUCH HELPER
        // Handles both drag to reorder and swipe to delete
        // -----------------------------------------------
        ItemTouchHelper helper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(
                        // Drag directions — up and down only for a list
                        ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                        // Swipe directions
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder target) {
                        int fromPosition = viewHolder.getBindingAdapterPosition();
                        int toPosition = target.getBindingAdapterPosition();

                        if (!adapter.canDragOver(fromPosition, toPosition)) {
                            return false;
                        }

                        adapter.onItemDragged(fromPosition, toPosition);
                        return true;
                    }

                    @Override
                    public void clearView(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder) {
                        super.clearView(recyclerView, viewHolder);
                        // Save after drag is complete
                        MainActivity.saveData(ListDetailActivity.this);
                    }

                    @Override
                    public boolean isLongPressDragEnabled() {
                        return false; // drag handle takes over
                    }

                    @Override
                    public int getSwipeDirs(@NonNull RecyclerView recyclerView,
                                            @NonNull RecyclerView.ViewHolder viewHolder) {
                        int position = viewHolder.getBindingAdapterPosition();
                        if (!adapter.isSwipeable(position)) return 0;
                        return super.getSwipeDirs(recyclerView, viewHolder);
                    }

                    @Override
                    public boolean canDropOver(@NonNull RecyclerView recyclerView,
                                               @NonNull RecyclerView.ViewHolder current,
                                               @NonNull RecyclerView.ViewHolder target) {
                        int fromPosition = current.getBindingAdapterPosition();
                        int toPosition = target.getBindingAdapterPosition();
                        return adapter.canDragOver(fromPosition, toPosition);
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder,
                                         int direction) {
                        int position = viewHolder.getBindingAdapterPosition();
                        if (position == RecyclerView.NO_POSITION) return;

                        ItemAdapter.SwipeDeleteResult result =
                                adapter.deleteAtPosition(position);

                        if (result == null) return;

                        Snackbar.make(recyclerView,
                                        "Item deleted",
                                        Snackbar.LENGTH_LONG)
                                .setAction("UNDO", v -> {
                                    result.section.getItems().add(
                                            result.indexInSection, result.item);
                                    adapter.refreshList();
                                    MainActivity.saveData(ListDetailActivity.this);
                                })
                                .show();
                    }

                    @Override
                    public void onChildDraw(@NonNull Canvas c,
                                            @NonNull RecyclerView recyclerView,
                                            @NonNull RecyclerView.ViewHolder viewHolder,
                                            float dX, float dY,
                                            int actionState,
                                            boolean isCurrentlyActive) {

                        // Only draw red swipe background for swipe actions
                        // not for drag actions
                        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                            View itemView = viewHolder.itemView;
                            Paint paint = new Paint();
                            paint.setColor(Color.RED);

                            if (dX > 0) {
                                c.drawRect(itemView.getLeft(),
                                        itemView.getTop(),
                                        itemView.getLeft() + dX,
                                        itemView.getBottom(), paint);
                            } else {
                                c.drawRect(itemView.getRight() + dX,
                                        itemView.getTop(),
                                        itemView.getRight(),
                                        itemView.getBottom(), paint);
                            }

                            Drawable icon = ResourcesCompat.getDrawable(
                                    getResources(), R.drawable.ic_delete, null);

                            if (icon != null) {
                                int iconMargin = (itemView.getHeight()
                                        - icon.getIntrinsicHeight()) / 2;
                                int iconTop = itemView.getTop() + iconMargin;
                                int iconBottom = iconTop + icon.getIntrinsicHeight();

                                if (dX > 0) {
                                    int iconLeft = itemView.getLeft() + iconMargin;
                                    int iconRight = iconLeft + icon.getIntrinsicWidth();
                                    icon.setBounds(iconLeft, iconTop,
                                            iconRight, iconBottom);
                                } else {
                                    int iconRight = itemView.getRight() - iconMargin;
                                    int iconLeft = iconRight - icon.getIntrinsicWidth();
                                    icon.setBounds(iconLeft, iconTop,
                                            iconRight, iconBottom);
                                }
                                icon.draw(c);
                            }
                        }

                        super.onChildDraw(c, recyclerView, viewHolder,
                                dX, dY, actionState, isCurrentlyActive);
                    }
                });

        helper.attachToRecyclerView(recyclerView);
        adapter.setItemTouchHelper(helper);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}