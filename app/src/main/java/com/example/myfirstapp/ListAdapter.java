package com.example.myfirstapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private final List<ShoppingList> lists;
    private final OnListLongClickListener longClickListener;

    public interface OnListLongClickListener {
        void onListLongClick(int position);
    }

    public ListAdapter(List<ShoppingList> lists,
                       OnListLongClickListener listener) {
        this.lists = lists;
        this.longClickListener = listener;
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
        holder.title.setText(list.getTitle());

        // Tap → open list
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ListDetailActivity.class);
            intent.putExtra("list_index", position);
            v.getContext().startActivity(intent);
        });

        // Long press → delete list
        holder.itemView.setOnLongClickListener(v -> {
            longClickListener.onListLongClick(position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.listTitle);
        }
    }
}
