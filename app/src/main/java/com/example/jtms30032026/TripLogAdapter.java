package com.example.jtms30032026;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TripLogAdapter extends RecyclerView.Adapter<TripLogAdapter.ViewHolder> {

    private List<TripLogItem> items = new ArrayList<>();

    public void setItems(List<TripLogItem> newItems) {
        items = new ArrayList<>(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trip_log, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TripLogItem item = items.get(position);
        holder.tvPlate.setText(item.plateNumber);
        holder.tvDriver.setText("Driver : " + item.driverName);
        holder.tvDepart.setText(item.departTime);
        holder.tvReturn.setText(item.returnTime);
        holder.tvCapacity.setText(String.valueOf(item.capacity));
        holder.tvFare.setText("₱" + item.fare);
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlate, tvDriver, tvDepart, tvReturn, tvCapacity, tvFare;

        ViewHolder(View v) {
            super(v);
            tvPlate    = v.findViewById(R.id.tvPlateNumber);
            tvDriver   = v.findViewById(R.id.tvDriverName);
            tvDepart   = v.findViewById(R.id.tvDepartTime);
            tvReturn   = v.findViewById(R.id.tvReturnTime);
            tvCapacity = v.findViewById(R.id.tvCapacity);
            tvFare     = v.findViewById(R.id.tvFare);
        }
    }
}