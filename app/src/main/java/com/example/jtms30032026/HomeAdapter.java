package com.example.jtms30032026;

import android.app.AlertDialog;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {

    private List<HomeModel> homeList;

    public interface OnTripActionListener {
        void onRefreshNeeded();
    }

    private OnTripActionListener listener;

    public HomeAdapter(List<HomeModel> homeList, OnTripActionListener listener) {
        this.homeList = homeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HomeModel jeepney = homeList.get(position);

        holder.tvPlateNumber.setText(jeepney.getPlate_number());
        holder.tvDriverName.setText(jeepney.getDriver_name());
        holder.tvLastActivity.setText(jeepney.getLast_activity());
        holder.tvTotalTrips.setText(jeepney.getTotal_trips());
        holder.tvCapacity.setText(jeepney.getCapacity());
        holder.tvTotalFare.setText("₱" + jeepney.getTotal_fare());

        // ── Status badge ──────────────────────────────────────────────────────
        if (jeepney.getStatus().equals("IN TERMINAL")) {
            holder.tvStatus.setText("● IN TERMINAL");
            holder.tvStatus.setBackgroundResource(R.drawable.status_background);
            holder.tvStatus.getBackground().setTint(Color.parseColor("#1B4332"));
            holder.btnDepartReturn.setText("Depart");
            holder.btnDepartReturn.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#1B4332")));
        } else {
            holder.tvStatus.setText("● ON ROAD");
            holder.tvStatus.setBackgroundResource(R.drawable.status_background);
            holder.tvStatus.getBackground().setTint(Color.parseColor("#E65100"));
            holder.btnDepartReturn.setText("Return");
            holder.btnDepartReturn.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#E65100")));
        }

        // ── Button click — show confirmation dialog ────────────────────────────
        holder.btnDepartReturn.setOnClickListener(v -> {
            if (jeepney.getStatus().equals("IN TERMINAL")) {
                showDepartConfirmDialog(v, jeepney);
            } else {
                showReturnConfirmDialog(v, jeepney);
            }
        });
    }

    // ─── Depart Confirmation Dialog ───────────────────────────────────────────

    private void showDepartConfirmDialog(View v, HomeModel jeepney) {
        new AlertDialog.Builder(v.getContext())
                .setTitle("Confirm Departure")
                .setMessage("Are you sure you want to depart jeepney\n" +
                        jeepney.getPlate_number() + " — " + jeepney.getDriver_name() + "?")
                .setPositiveButton("Yes, Depart", (dialog, which) -> recordDepart(v, jeepney))
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ─── Return Confirmation Dialog ───────────────────────────────────────────

    private void showReturnConfirmDialog(View v, HomeModel jeepney) {
        new AlertDialog.Builder(v.getContext())
                .setTitle("Confirm Return")
                .setMessage("Are you sure you want to record return for\n" +
                        jeepney.getPlate_number() + " — " + jeepney.getDriver_name() + "?")
                .setPositiveButton("Yes, Return", (dialog, which) -> recordReturn(v, jeepney))
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ─── Record Depart ────────────────────────────────────────────────────────

    private void recordDepart(View v, HomeModel jeepney) {
        String url = AppConfig.BASE_URL + "tripdepart.php";
        RequestQueue queue = Volley.newRequestQueue(v.getContext());
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    if (!response.trim().equals("failed") && !response.trim().equals("failed to connect")) {
                        Toast.makeText(v.getContext(), "Jeepney Departed!", Toast.LENGTH_SHORT).show();
                        listener.onRefreshNeeded();
                    } else {
                        Toast.makeText(v.getContext(), "Failed to record depart", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Log.e("VolleyError", error.toString())
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("jeepney_id", jeepney.getJeepney_id());
                return params;
            }
        };
        queue.add(request);
    }

    // ─── Record Return ────────────────────────────────────────────────────────

    private void recordReturn(View v, HomeModel jeepney) {
        Log.d("TripDebug", "Sending trip_id: " + jeepney.getActive_trip_id()
                + " jeepney_id: " + jeepney.getJeepney_id());
        String url = AppConfig.BASE_URL + "tripreturn.php";
        RequestQueue queue = Volley.newRequestQueue(v.getContext());
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    if (response.trim().equals("success")) {
                        Toast.makeText(v.getContext(), "Jeepney Returned!", Toast.LENGTH_SHORT).show();
                        listener.onRefreshNeeded();
                    } else {
                        Toast.makeText(v.getContext(), "Failed to record return: " + response,
                                Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Log.e("VolleyError", error.toString())
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("trip_id", jeepney.getActive_trip_id());
                params.put("jeepney_id", jeepney.getJeepney_id());
                return params;
            }
        };
        queue.add(request);
    }

    // ─── FIFO Sort ────────────────────────────────────────────────────────────
    // Departed jeepneys (ON ROAD) sorted by earliest depart time first.
    // IN TERMINAL jeepneys go to the bottom, sorted by jeepney_id.

    public void updateList(List<HomeModel> newList) {
        List<HomeModel> departed  = new ArrayList<>();
        List<HomeModel> terminal  = new ArrayList<>();

        for (HomeModel m : newList) {
            if (m.getStatus().equals("DEPARTED") || m.getStatus().equals("ON ROAD")) {
                departed.add(m);
            } else {
                terminal.add(m);
            }
        }

        // Sort departed by active_trip_id ascending (lower trip_id = departed earlier = FIFO)
        Collections.sort(departed, (a, b) -> {
            try {
                return Integer.parseInt(a.getActive_trip_id()) - Integer.parseInt(b.getActive_trip_id());
            } catch (NumberFormatException e) {
                return 0;
            }
        });

        // Sort terminal by jeepney_id ascending
        Collections.sort(terminal, (a, b) -> {
            try {
                return Integer.parseInt(a.getJeepney_id()) - Integer.parseInt(b.getJeepney_id());
            } catch (NumberFormatException e) {
                return 0;
            }
        });

        homeList = new ArrayList<>();
        homeList.addAll(departed);
        homeList.addAll(terminal);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return homeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlateNumber, tvDriverName, tvStatus, tvLastActivity,
                tvTotalTrips, tvTotalFare, tvCapacity;
        Button btnDepartReturn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlateNumber  = itemView.findViewById(R.id.tvHomePlateNumber);
            tvDriverName   = itemView.findViewById(R.id.tvHomeDriverName);
            tvStatus       = itemView.findViewById(R.id.tvHomeStatus);
            tvLastActivity = itemView.findViewById(R.id.tvHomeLastActivity);
            tvTotalTrips   = itemView.findViewById(R.id.tvHomeTotalTrips);
            tvTotalFare    = itemView.findViewById(R.id.tvHomeTotalFare);
            tvCapacity     = itemView.findViewById(R.id.tvHomeCapacity);
            btnDepartReturn = itemView.findViewById(R.id.btnDepartReturn);
        }
    }
}