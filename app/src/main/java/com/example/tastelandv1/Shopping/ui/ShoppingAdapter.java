package com.example.tastelandv1.Shopping.ui;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tastelandv1.R;
import com.example.tastelandv1.Shopping.database.ShoppingItem;

import java.util.List;

public class ShoppingAdapter extends RecyclerView.Adapter<ShoppingAdapter.ViewHolder> {

    private List<ShoppingItem> items;
    private OnItemChangeListener listener;

    public interface OnItemChangeListener {
        void onItemChanged(ShoppingItem item);
    }

    public ShoppingAdapter(List<ShoppingItem> items, OnItemChangeListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shopping_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShoppingItem item = items.get(position);

        holder.tvItemText.setText(item.getText());
        holder.tvItemText.setFocusable(false);
        holder.tvItemText.setClickable(false);

        holder.cbItem.setOnCheckedChangeListener(null);
        holder.cbItem.setChecked(item.isChecked());

        // Visual Strikethrough
        if (item.isChecked()) {
            holder.tvItemText.setPaintFlags(holder.tvItemText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvItemText.setPaintFlags(holder.tvItemText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        holder.cbItem.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setChecked(isChecked);

            // Visual Strikethrough update
            if (isChecked) {
                holder.tvItemText.setPaintFlags(holder.tvItemText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                holder.tvItemText.setPaintFlags(holder.tvItemText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }

            if (listener != null) {
                listener.onItemChanged(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbItem;
        TextView tvItemText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cbItem = itemView.findViewById(R.id.cb_item);
            tvItemText = itemView.findViewById(R.id.et_item_text);
        }
    }
}