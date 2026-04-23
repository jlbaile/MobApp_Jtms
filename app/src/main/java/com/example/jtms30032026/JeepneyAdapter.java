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

public class JeepneyAdapter extends RecyclerView.Adapter<JeepneyAdapter.ViewHolder> {

    private List<JeepneyModel> jeepneyList;
    private List<JeepneyModel> jeepneyListFull;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditClick(JeepneyModel jeepney);
        void onDeleteClick(JeepneyModel jeepney);
    }

    public JeepneyAdapter(List<JeepneyModel> jeepneyList, OnItemClickListener listener) {
        this.jeepneyListFull = new ArrayList<>(jeepneyList);
        this.jeepneyList     = new ArrayList<>(jeepneyList);
        this.listener        = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_jeepney, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JeepneyModel jeepney = jeepneyList.get(position);

        holder.tvDriverName.setText(jeepney.getDriver_name());
        holder.tvPlateNumber.setText(jeepney.getPlate_number());
        holder.tvCapacity.setText(jeepney.getCapacity() + " Seater");

        // Kebab menu — Edit or Delete
        holder.btnItemMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenu().add(0, 1, 0, "✏️  Edit");
            popup.getMenu().add(0, 2, 1, "🗑️  Delete");

            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) {
                    if (listener != null) listener.onEditClick(jeepney);
                    return true;
                } else if (item.getItemId() == 2) {
                    if (listener != null) listener.onDeleteClick(jeepney);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return jeepneyList.size();
    }

    public void updateList(List<JeepneyModel> newList) {
        jeepneyListFull = new ArrayList<>(newList);
        jeepneyList     = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        jeepneyList.clear();
        if (query.isEmpty()) {
            jeepneyList.addAll(jeepneyListFull);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (JeepneyModel item : jeepneyListFull) {
                if (item.getDriver_name().toLowerCase().contains(lowerQuery) ||
                        item.getPlate_number().toLowerCase().contains(lowerQuery)) {
                    jeepneyList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView  tvDriverName, tvPlateNumber, tvCapacity, tvStatus;
        ImageView btnItemMenu;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDriverName  = itemView.findViewById(R.id.tvItemDriverName);
            tvPlateNumber = itemView.findViewById(R.id.tvItemPlateNumber);
            tvCapacity    = itemView.findViewById(R.id.tvItemCapacity);
            tvStatus      = itemView.findViewById(R.id.tvItemStatus);
            btnItemMenu   = itemView.findViewById(R.id.btnItemMenu);
        }
    }
}