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
            holder.btnDepartReturn.setSingleLine(true);
            holder.btnDepartReturn.setEnabled(true);
            holder.btnDepartReturn.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#1B4332")));

        } else {
            holder.tvStatus.setText("● ON ROAD");
            holder.tvStatus.setBackgroundResource(R.drawable.status_background);
            holder.tvStatus.getBackground().setTint(Color.parseColor("#E65100"));

            boolean isAdmin    = SessionManager.getInstance().isAdmin();
            String currentUser = SessionManager.getInstance().getLoggedInUsername();
            String departedBy  = jeepney.getDeparted_by(); // username stored in DB

            // Build the @Username label for display
            String label = (departedBy == null || departedBy.isEmpty())
                    ? ""
                    : (departedBy.equalsIgnoreCase("admin") ? "@Admin" : "@" + departedBy);

            boolean canReturn = isAdmin
                    || departedBy == null
                    || departedBy.isEmpty()
                    || departedBy.equals(currentUser);

            if (canReturn) {
                // Admin always sees who departed + can still press Return
                // Staff who departed it also sees their own name + can press Return
                String btnText = (label.isEmpty()) ? "Return" : "Return · " + label;
                holder.btnDepartReturn.setText(btnText);
                holder.btnDepartReturn.setSingleLine(true);
                holder.btnDepartReturn.setEnabled(true);
                holder.btnDepartReturn.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(Color.parseColor("#E65100")));
            } else {
                // Other staff — locked, shows who departed
                holder.btnDepartReturn.setText("Departed by " + label);
                holder.btnDepartReturn.setSingleLine(true);
                holder.btnDepartReturn.setEnabled(false);
                holder.btnDepartReturn.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }
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
        View dialogView = LayoutInflater.from(v.getContext())
                .inflate(R.layout.dialog_confirm_depart, null);

        TextView tvMessage = dialogView.findViewById(R.id.tvDepartMessage);
        tvMessage.setText("Are you sure you want to depart jeepney "
                + jeepney.getPlate_number() + " — " + jeepney.getDriver_name() + "?");

        AlertDialog dialog = new AlertDialog.Builder(v.getContext())
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        Button btnCancel  = dialogView.findViewById(R.id.btnCancelDepart);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmDepart);

        btnCancel.setOnClickListener(cancel -> dialog.dismiss());
        btnConfirm.setOnClickListener(confirm -> {
            dialog.dismiss();
            recordDepart(v, jeepney);
        });

        dialog.show();
    }

    // ─── Return Confirmation Dialog ───────────────────────────────────────────

    private void showReturnConfirmDialog(View v, HomeModel jeepney) {
        View dialogView = LayoutInflater.from(v.getContext())
                .inflate(R.layout.dialog_confirm_return, null);

        TextView tvMessage = dialogView.findViewById(R.id.tvReturnMessage);
        tvMessage.setText("Are you sure you want to record return for "
                + jeepney.getPlate_number() + " — " + jeepney.getDriver_name() + "?");

        AlertDialog dialog = new AlertDialog.Builder(v.getContext())
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        Button btnCancel  = dialogView.findViewById(R.id.btnCancelReturn);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmReturn);

        btnCancel.setOnClickListener(cancel -> dialog.dismiss());
        btnConfirm.setOnClickListener(confirm -> {
            dialog.dismiss();
            recordReturn(v, jeepney);
        });

        dialog.show();
    }

    // ─── Record Depart ────────────────────────────────────────────────────────

    private void recordDepart(View v, HomeModel jeepney) {
        final String departedBy = SessionManager.getInstance().getLoggedInUsername();
        Log.d("DepartDebug", "departed_by being sent: " + departedBy);

        String url = AppConfig.BASE_URL + "tripdepart.php";
        RequestQueue queue = Volley.newRequestQueue(v.getContext());

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("DepartDebug", "tripdepart.php response: " + response.trim());
                    String res = response.trim();
                    if (res.equals("already_departed")) {
                        Toast.makeText(v.getContext(),
                                "This jeepney was already departed by someone else.",
                                Toast.LENGTH_LONG).show();
                        listener.onRefreshNeeded();
                    } else if (!res.equals("failed") && !res.equals("failed to connect")) {
                        Toast.makeText(v.getContext(), "Jeepney Departed!", Toast.LENGTH_SHORT).show();
                        listener.onRefreshNeeded();
                    } else {
                        Toast.makeText(v.getContext(), "Failed to record depart",
                                Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Log.e("VolleyError", error.toString())
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("jeepney_id",  jeepney.getJeepney_id());
                params.put("departed_by", departedBy);
                return params;
            }
        };

        queue.add(request);
    }

    // ─── Record Return ────────────────────────────────────────────────────────

    private void recordReturn(View v, HomeModel jeepney) {
        final String returnedBy = SessionManager.getInstance().getLoggedInUsername();
        Log.d("ReturnDebug", "Sending trip_id: " + jeepney.getActive_trip_id()
                + " | jeepney_id: " + jeepney.getJeepney_id()
                + " | returned_by: " + returnedBy);

        String url = AppConfig.BASE_URL + "tripreturn.php";
        RequestQueue queue = Volley.newRequestQueue(v.getContext());

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("ReturnDebug", "tripreturn.php response: " + response.trim());
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
                params.put("trip_id",     jeepney.getActive_trip_id());
                params.put("jeepney_id",  jeepney.getJeepney_id());
                params.put("returned_by", returnedBy);
                return params;
            }
        };

        queue.add(request);
    }

    // ─── FIFO Sort ────────────────────────────────────────────────────────────

    public void updateList(List<HomeModel> newList) {
        List<HomeModel> departed = new ArrayList<>();
        List<HomeModel> terminal = new ArrayList<>();

        for (HomeModel m : newList) {
            if (m.getStatus().equals("DEPARTED") || m.getStatus().equals("ON ROAD")) {
                departed.add(m);
            } else {
                terminal.add(m);
            }
        }

        Collections.sort(departed, (a, b) -> {
            try {
                return Integer.parseInt(a.getActive_trip_id())
                        - Integer.parseInt(b.getActive_trip_id());
            } catch (NumberFormatException e) {
                return 0;
            }
        });

        Collections.sort(terminal, (a, b) -> {
            try {
                return Integer.parseInt(a.getJeepney_id())
                        - Integer.parseInt(b.getJeepney_id());
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
            tvPlateNumber   = itemView.findViewById(R.id.tvHomePlateNumber);
            tvDriverName    = itemView.findViewById(R.id.tvHomeDriverName);
            tvStatus        = itemView.findViewById(R.id.tvHomeStatus);
            tvLastActivity  = itemView.findViewById(R.id.tvHomeLastActivity);
            tvTotalTrips    = itemView.findViewById(R.id.tvHomeTotalTrips);
            tvTotalFare     = itemView.findViewById(R.id.tvHomeTotalFare);
            tvCapacity      = itemView.findViewById(R.id.tvHomeCapacity);
            btnDepartReturn = itemView.findViewById(R.id.btnDepartReturn);
        }
    }
}