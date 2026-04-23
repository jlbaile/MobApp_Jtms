package com.example.jtms30032026;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.ViewHolder> {

    private List<StaffModel> staffList;
    private List<StaffModel> staffListFull;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditClick(StaffModel staff);
        void onDeleteClick(StaffModel staff);
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

        holder.tvStaffName.setText(staff.getStaff_fname() + " " + staff.getStaff_lname());
        holder.tvStaffUsername.setText("@" + staff.getStaff_username());

        // Kebab menu — shows Edit and Delete options
        holder.btnStaffItemMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenu().add(0, 1, 0, "✏️  Edit");
            popup.getMenu().add(0, 2, 1, "🗑️  Delete");

            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) {
                    if (listener != null) listener.onEditClick(staff);
                    return true;
                } else if (item.getItemId() == 2) {
                    if (listener != null) listener.onDeleteClick(staff);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return staffList.size();
    }

    public void updateList(List<StaffModel> newList) {
        staffListFull = new ArrayList<>(newList);
        staffList     = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        staffList.clear();
        if (query.isEmpty()) {
            staffList.addAll(staffListFull);
        } else {
            String lower = query.toLowerCase().trim();
            for (StaffModel item : staffListFull) {
                if (item.getStaff_fname().toLowerCase().contains(lower) ||
                        item.getStaff_lname().toLowerCase().contains(lower) ||
                        item.getStaff_username().toLowerCase().contains(lower)) {
                    staffList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView  tvStaffName, tvStaffUsername;
        ImageView btnStaffItemMenu;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStaffName       = itemView.findViewById(R.id.tvItemStaffName);
            tvStaffUsername   = itemView.findViewById(R.id.tvItemStaffUsername);
            btnStaffItemMenu  = itemView.findViewById(R.id.btnStaffItemMenu);
        }
    }
}