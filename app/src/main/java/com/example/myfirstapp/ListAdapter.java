package com.example.myfirstapp;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.Collections;
import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private final List<ShoppingList> lists;
    private final OnListLongClickListener longClickListener;

    // Maximum number of item previews shown on a card
    private static final int MAX_PREVIEW_ITEMS = 3;

    // Reference to ItemTouchHelper so the drag handle can start a drag
    private ItemTouchHelper itemTouchHelper;

    // Callback to notify MainActivity to save after a drag is complete
    public interface OnListReorderedListener {
        void onListReordered();
    }

    private final OnListReorderedListener reorderedListener;

    public interface OnListLongClickListener {
        void onListLongClick(int position);
    }

    public ListAdapter(List<ShoppingList> lists,
                       OnListLongClickListener longClickListener,
                       OnListReorderedListener reorderedListener) {
        this.lists = lists;
        this.longClickListener = longClickListener;
        this.reorderedListener = reorderedListener;
    }

    // Called from MainActivity after attaching ItemTouchHelper to RecyclerView
    public void setItemTouchHelper(ItemTouchHelper itemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShoppingList list = lists.get(position);

        // -----------------------------------------------
        // TITLE
        // -----------------------------------------------
        holder.title.setText(list.getTitle());

        // -----------------------------------------------
        // RECURRENCE LABEL
        // -----------------------------------------------
        String recurrenceLabel = getRecurrenceLabel(list);

        if (recurrenceLabel != null) {
            holder.recurrence.setText(recurrenceLabel);
            holder.recurrence.setVisibility(View.VISIBLE);
        } else {
            holder.recurrence.setVisibility(View.GONE);
        }

        // -----------------------------------------------
        // ITEM PREVIEW
        // -----------------------------------------------
        bindItemPreview(holder, list);

        // -----------------------------------------------
        // FAVORITE STATE
        // Star icon and card color update together
        // Both branches always explicitly set to prevent
        // stale state from RecyclerView view recycling
        // -----------------------------------------------
        if (list.isFavorite()) {
            holder.favoriteButton.setImageResource(R.drawable.ic_star_filled);
            holder.favoriteButton.setColorFilter(
                    ContextCompat.getColor(
                            holder.itemView.getContext(), R.color.accent_gold));
            holder.card.setCardBackgroundColor(
                    ContextCompat.getColor(
                            holder.itemView.getContext(), R.color.card_favorite));
        } else {
            holder.favoriteButton.setImageResource(R.drawable.ic_star_outline);
            holder.favoriteButton.setColorFilter(
                    ContextCompat.getColor(
                            holder.itemView.getContext(), R.color.text_secondary));
            holder.card.setCardBackgroundColor(
                    ContextCompat.getColor(
                            holder.itemView.getContext(), R.color.card_dark));
        }

        // -----------------------------------------------
        // STAR TAP → TOGGLE FAVORITE
        // setClickable ensures button captures touch
        // before the card click listener does
        // setFocusable false prevents focus interference
        // notifyDataSetChanged forces immediate redraw
        // -----------------------------------------------

        holder.favoriteButton.setClickable(true);
        holder.favoriteButton.setFocusable(false);
        holder.favoriteButton.setOnClickListener(v -> {
            list.setFavorite(!list.isFavorite());
            notifyDataSetChanged();
            if (reorderedListener != null) {
                reorderedListener.onListReordered();
            }
        });


        // -----------------------------------------------
        // DRAG HANDLE
        // -----------------------------------------------
        holder.dragHandle.setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                if (itemTouchHelper != null) {
                    itemTouchHelper.startDrag(holder);
                }
            }
            return false;
        });

        // -----------------------------------------------
        // CLICK LISTENERS
        // -----------------------------------------------

        // Tap → open list using master index to avoid stale position bug
        holder.itemView.setOnClickListener(v -> {
            int masterIndex = MainActivity.shoppingLists.indexOf(list);
            if (masterIndex != -1) {
                Intent intent = new Intent(v.getContext(), ListDetailActivity.class);
                intent.putExtra("list_index", masterIndex);
                v.getContext().startActivity(intent);
            }
        });

        // Long press → options menu
        holder.itemView.setOnLongClickListener(v -> {
            longClickListener.onListLongClick(position);
            return true;
        });
    }

    // -----------------------------------------------
    // Called by ItemTouchHelper when a card is dragged
    // -----------------------------------------------
    public void onItemMoved(int fromPosition, int toPosition) {

        Collections.swap(lists, fromPosition, toPosition);

        ShoppingList fromList = lists.get(toPosition);
        ShoppingList toList = lists.get(fromPosition);

        int fromIndex = MainActivity.shoppingLists.indexOf(fromList);
        int toIndex = MainActivity.shoppingLists.indexOf(toList);

        if (fromIndex != -1 && toIndex != -1) {
            Collections.swap(MainActivity.shoppingLists, fromIndex, toIndex);
        }

        notifyItemMoved(fromPosition, toPosition);
    }

    // -----------------------------------------------
    // Called by ItemTouchHelper when drag is complete
    // -----------------------------------------------
    public void onItemDropped() {
        if (reorderedListener != null) {
            reorderedListener.onListReordered();
        }
    }

    // -----------------------------------------------
    // Binds item preview TextViews on the card
    // -----------------------------------------------
    private void bindItemPreview(ViewHolder holder, ShoppingList list) {

        List<ShoppingItem> items = list.getItems();

        TextView[] previewViews = {
                holder.previewItem1,
                holder.previewItem2,
                holder.previewItem3
        };

        if (items == null || items.isEmpty()) {
            holder.previewDivider.setVisibility(View.GONE);
            for (TextView tv : previewViews) {
                tv.setVisibility(View.GONE);
            }
            holder.previewOverflow.setVisibility(View.GONE);
            return;
        }

        holder.previewDivider.setVisibility(View.VISIBLE);

        int totalItems = items.size();

        for (int i = 0; i < MAX_PREVIEW_ITEMS; i++) {
            if (i < totalItems) {
                ShoppingItem item = items.get(i);
                String prefix = item.isChecked() ? "✓ " : "• ";
                previewViews[i].setText(prefix + item.getName());
                previewViews[i].setVisibility(View.VISIBLE);

                if (item.isChecked()) {
                    previewViews[i].setAlpha(0.45f);
                } else {
                    previewViews[i].setAlpha(1.0f);
                }
            } else {
                previewViews[i].setVisibility(View.GONE);
            }
        }

        int remaining = totalItems - MAX_PREVIEW_ITEMS;

        if (remaining > 0) {
            holder.previewOverflow.setText("+" + remaining + " more...");
            holder.previewOverflow.setVisibility(View.VISIBLE);
        } else {
            holder.previewOverflow.setVisibility(View.GONE);
        }
    }

    // -----------------------------------------------
    // Builds the human-readable recurrence string
    // -----------------------------------------------
    private String getRecurrenceLabel(ShoppingList list) {

        if (list.getRecurringType() == null) return null;

        switch (list.getRecurringType()) {
            case "weekly":
                return "↻ Every " + list.getRecurringValue();
            case "monthly":
                return "↻ Every " + getOrdinal(list.getRecurringValue()) + " of the month";
            case "yearly":
                return "↻ Every " + formatYearlyDate(list.getRecurringValue());
            default:
                return null;
        }
    }

    // -----------------------------------------------
    // Converts "03-25" → "March 25"
    // -----------------------------------------------
    private String formatYearlyDate(String mmDd) {

        if (mmDd == null || mmDd.isEmpty()) return "";

        try {
            java.text.SimpleDateFormat inputFormat =
                    new java.text.SimpleDateFormat("MM-dd", java.util.Locale.getDefault());
            java.text.SimpleDateFormat outputFormat =
                    new java.text.SimpleDateFormat("MMMM d", java.util.Locale.getDefault());
            java.util.Date date = inputFormat.parse(mmDd);
            return date != null ? outputFormat.format(date) : mmDd;
        } catch (java.text.ParseException e) {
            return mmDd;
        }
    }

    // -----------------------------------------------
    // Converts "1" → "1st", "2" → "2nd", etc.
    // -----------------------------------------------
    private String getOrdinal(String numberStr) {

        if (numberStr == null || numberStr.isEmpty()) return numberStr;

        try {
            int n = Integer.parseInt(numberStr.trim());
            if (n >= 11 && n <= 13) return n + "th";
            switch (n % 10) {
                case 1: return n + "st";
                case 2: return n + "nd";
                case 3: return n + "rd";
                default: return n + "th";
            }
        } catch (NumberFormatException e) {
            return numberStr;
        }
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        TextView title;
        TextView recurrence;
        ImageButton favoriteButton;
        ImageView dragHandle;
        View previewDivider;
        TextView previewItem1;
        TextView previewItem2;
        TextView previewItem3;
        TextView previewOverflow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.listCard);
            title = itemView.findViewById(R.id.listTitle);
            recurrence = itemView.findViewById(R.id.listRecurrence);
            favoriteButton = itemView.findViewById(R.id.favoriteButton);
            dragHandle = itemView.findViewById(R.id.dragHandle);
            previewDivider = itemView.findViewById(R.id.previewDivider);
            previewItem1 = itemView.findViewById(R.id.previewItem1);
            previewItem2 = itemView.findViewById(R.id.previewItem2);
            previewItem3 = itemView.findViewById(R.id.previewItem3);
            previewOverflow = itemView.findViewById(R.id.previewOverflow);
        }
    }
}
