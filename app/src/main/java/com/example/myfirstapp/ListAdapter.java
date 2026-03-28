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

        // Tap → open list
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ListDetailActivity.class);
            intent.putExtra("list_index", position);
            v.getContext().startActivity(intent);
        });

        // Long press → archive / delete options
        holder.itemView.setOnLongClickListener(v -> {
            longClickListener.onListLongClick(position);
            return true;
        });
    }

    // -----------------------------------------------
    // Builds the human-readable recurrence string
    // Returns null if the list is not recurring
    // -----------------------------------------------
    private String getRecurrenceLabel(ShoppingList list) {

        if (list.getRecurringType() == null) return null;

        switch (list.getRecurringType()) {

            case "weekly":
                // e.g. "↻ Every Sunday"
                return "↻ Every " + list.getRecurringValue();

            case "monthly":
                // e.g. "↻ Every 1st of the month"
                return "↻ Every " + getOrdinal(list.getRecurringValue()) + " of the month";

            case "yearly":
                // e.g. "↻ Every March 25"
                return "↻ Every " + formatYearlyDate(list.getRecurringValue());

            default:
                // "none" or anything unexpected → no label
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
            // If parsing fails, fall back to raw value
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

            // Special cases: 11th, 12th, 13th
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
        TextView title;
        TextView recurrence;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.listTitle);
            recurrence = itemView.findViewById(R.id.listRecurrence);
        }
    }
}
