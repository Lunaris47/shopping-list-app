package com.example.myfirstapp;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.CheckBox;

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

        holder.itemCheckbox.setOnCheckedChangeListener(null);
        holder.itemCheckbox.setChecked(item.isChecked());

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

        holder.itemCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {

            item.setChecked(isChecked);

            notifyItemChanged(position);

            // Save changes
            onItemChanged.run();
        });

        // Allow tapping anywhere on the card
        holder.itemView.setOnClickListener(v -> holder.itemCheckbox.toggle());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView itemText;
        CheckBox itemCheckbox;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemText = itemView.findViewById(R.id.itemText);
            itemCheckbox = itemView.findViewById(R.id.itemCheckbox);
        }
    }
}