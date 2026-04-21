package com.example.jtms30032026;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {

    private List<ReportItem> items;

    public ReportAdapter(List<ReportItem> items) {
        this.items = items;
    }

    public void updateData(List<ReportItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReportItem item = items.get(position);
        holder.tvPlate.setText(item.getPlateNumber());
        holder.tvDriver.setText(item.getDriverName());
        holder.tvTrips.setText(String.valueOf(item.getTripCount()));
        holder.tvPassengers.setText(String.valueOf(item.getEstPassengers()));
        holder.tvRevenue.setText(String.format("₱%.2f", item.getRevenue()));
        holder.tvCapacity.setText("Cap: " + item.getCapacity());
    }

    @Override
    public int getItemCount() { return items != null ? items.size() : 0; }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlate, tvDriver, tvTrips, tvPassengers, tvRevenue, tvCapacity;

        ViewHolder(View itemView) {
            super(itemView);
            tvPlate      = itemView.findViewById(R.id.tvReportPlate);
            tvDriver     = itemView.findViewById(R.id.tvReportDriver);
            tvTrips      = itemView.findViewById(R.id.tvReportTrips);
            tvPassengers = itemView.findViewById(R.id.tvReportPassengers);
            tvRevenue    = itemView.findViewById(R.id.tvReportRevenue);
            tvCapacity   = itemView.findViewById(R.id.tvReportCapacity);
        }
    }
}