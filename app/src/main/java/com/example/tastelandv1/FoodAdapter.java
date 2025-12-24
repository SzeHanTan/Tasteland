package com.example.tastelandv1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {
    private List<FoodItem> foodList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onCheckedChange(FoodItem item, boolean isChecked);
    }

    public FoodAdapter(List<FoodItem> foodList, OnItemClickListener listener) {
        this.foodList = foodList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodItem item = foodList.get(position);
        holder.tvName.setText(item.name);
        holder.cbFinished.setChecked(item.isFinished);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
        holder.tvDate.setText(sdf.format(item.dueDate));

        // When the checkbox is clicked
        holder.cbFinished.setOnClickListener(v -> {
            boolean isChecked = holder.cbFinished.isChecked();
            if (isChecked) {
                // Trigger the listener to delete the item
                listener.onCheckedChange(item, true);
            }
        });
    }


    @Override
    public int getItemCount() { return foodList.size(); }

    static class FoodViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDate;
        CheckBox cbFinished;
        FoodViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.TVFoodName);
            tvDate = v.findViewById(R.id.TVFoodDate);
            cbFinished = v.findViewById(R.id.CBFinished);
        }
    }
}

