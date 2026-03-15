package com.example.myfirstapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ArchivedListAdapter extends RecyclerView.Adapter<ArchivedListAdapter.ViewHolder> {

    private final List<ShoppingList> lists;
    private final OnRestoreClickListener listener;

    public interface OnRestoreClickListener {
        void onRestore(ShoppingList list);
    }

    public ArchivedListAdapter(List<ShoppingList> lists, OnRestoreClickListener listener) {
        this.lists = lists;
        this.listener = listener;
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

        holder.itemView.setOnClickListener(v -> listener.onRestore(list));
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.listTitle);
        }
    }
}