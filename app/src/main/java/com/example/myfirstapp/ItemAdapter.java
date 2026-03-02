package com.example.myfirstapp;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private final List<ShoppingItem> items;
    private final Runnable onItemChanged;

    public ItemAdapter(List<ShoppingItem> items, Runnable onItemChanged) {
        this.items = items;
        this.onItemChanged = onItemChanged;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        ShoppingItem item = items.get(position);

        holder.itemText.setText(item.getName());

        // Apply or remove strikethrough
        if (item.isChecked()) {
            holder.itemText.setPaintFlags(
                    holder.itemText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
            );
        } else {
            holder.itemText.setPaintFlags(
                    holder.itemText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG)
            );
        }

        holder.itemView.setOnClickListener(v -> {

            // Toggle checked state
            item.toggleChecked();

            // Reorder so checked go to bottom
            items.sort((a, b) -> {
                if (a.isChecked() == b.isChecked()) return 0;
                return a.isChecked() ? 1 : -1;
            });

            notifyDataSetChanged();

            // Trigger persistence save
            onItemChanged.run();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView itemText;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemText = itemView.findViewById(R.id.itemText);
        }
    }
}