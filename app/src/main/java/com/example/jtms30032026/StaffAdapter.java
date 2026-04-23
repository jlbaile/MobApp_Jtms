package com.example.jtms30032026;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.ViewHolder> {

    private List<StaffModel> staffList;
    private List<StaffModel> staffListFull;
    private OnItemClickListener listener;

    // ✅ Interface for click events — StaffFragment implements this
    public interface OnItemClickListener {
        void onItemClick(StaffModel staff);
    }

    public StaffAdapter(List<StaffModel> staffList, OnItemClickListener listener) {
        this.staffListFull = new ArrayList<>(staffList);
        this.staffList     = new ArrayList<>(staffList);
        this.listener      = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_staff, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StaffModel staff = staffList.get(position);
        holder.tvStaffName.setText("Name: " + staff.getStaff_fname() + " " + staff.getStaff_lname());
        holder.tvStaffUsername.setText("Username: " + staff.getStaff_username());

        // ✅ Trigger edit dialog on item click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(staff);
        });
    }

    @Override
    public int getItemCount() {
        return staffList.size();
    }

    public void filter(String query) {
        staffList.clear();
        if (query.isEmpty()) {
            staffList.addAll(staffListFull);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (StaffModel item : staffListFull) {
                if (item.getStaff_fname().toLowerCase().contains(lowerQuery) ||
                        item.getStaff_lname().toLowerCase().contains(lowerQuery)) {
                    staffList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void updateList(List<StaffModel> newList) {
        staffListFull = new ArrayList<>(newList);
        staffList     = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStaffName, tvStaffUsername;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStaffName     = itemView.findViewById(R.id.tvItemStaffName);
            tvStaffUsername = itemView.findViewById(R.id.tvItemStaffUsername);
        }
    }
}