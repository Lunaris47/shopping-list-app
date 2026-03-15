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

        holder.itemView.setOnLongClickListener(v -> {

            String[] options = {"Restore List", "Delete List"};

            new android.app.AlertDialog.Builder(v.getContext())
                    .setTitle(list.getTitle())
                    .setItems(options, (dialog, which) -> {

                        if (which == 0) { // Restore list

                            list.setArchived(false);

                            MainActivity.saveData(v.getContext());

                            lists.remove(list);
                            notifyDataSetChanged();

                            // Return to the main screen so it refreshes
                            ((android.app.Activity) v.getContext()).finish();
                        }

                        else if (which == 1) { // Delete

                            MainActivity.shoppingLists.remove(list);
                            MainActivity.saveData(v.getContext());

                            lists.remove(list);
                            notifyDataSetChanged();
                        }

                    })
                    .show();

            return true;
        });
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