package com.example.jtms30032026;

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
        holder.tvDriverName.setText("Driver: " + jeepney.getDriver_name());
        holder.tvLastActivity.setText(jeepney.getLast_activity());
        holder.tvTotalTrips.setText("Total Trips: " + jeepney.getTotal_trips());
        holder.tvTotalTrips.setText("Total Trips: " + jeepney.getTotal_trips());
        holder.tvCapacity.setText("Capacity: " + jeepney.getCapacity()); // Add this
        holder.tvStatus.setText(jeepney.getStatus());
        holder.tvTotalFare.setText("Total Fare: ₱" + jeepney.getTotal_fare()); // <- Fare display

        // Status color
        if (jeepney.getStatus().equals("IN TERMINAL")) {
            holder.tvStatus.setBackgroundResource(R.drawable.status_background);
            holder.tvStatus.getBackground().setTint(Color.parseColor("#4CAF50"));
            holder.btnDepartReturn.setText("Depart");
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.status_background);
            holder.tvStatus.getBackground().setTint(Color.parseColor("#F44336"));
            holder.btnDepartReturn.setText("Return");
        }

        holder.btnDepartReturn.setOnClickListener(v -> {
            if (jeepney.getStatus().equals("IN TERMINAL")) {
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

            } else {
                Log.d("TripDebug", "Sending trip_id: " + jeepney.getActive_trip_id() + " jeepney_id: " + jeepney.getJeepney_id());
                String url = AppConfig.BASE_URL + "tripreturn.php";
                RequestQueue queue = Volley.newRequestQueue(v.getContext());
                StringRequest request = new StringRequest(Request.Method.POST, url,
                        response -> {
                            if (response.trim().equals("success")) {
                                Toast.makeText(v.getContext(), "Jeepney Returned!", Toast.LENGTH_SHORT).show();
                                listener.onRefreshNeeded();
                            } else {
                                Toast.makeText(v.getContext(), "Failed to record return: " + response, Toast.LENGTH_SHORT).show();
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
        });
    }

    @Override
    public int getItemCount() {
        return homeList.size();
    }

    public void updateList(List<HomeModel> newList) {
        homeList = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlateNumber, tvDriverName, tvStatus, tvLastActivity, tvTotalTrips, tvTotalFare, tvCapacity; // Add tvCapacity
        Button btnDepartReturn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlateNumber = itemView.findViewById(R.id.tvHomePlateNumber);
            tvDriverName = itemView.findViewById(R.id.tvHomeDriverName);
            tvStatus = itemView.findViewById(R.id.tvHomeStatus);
            tvLastActivity = itemView.findViewById(R.id.tvHomeLastActivity);
            tvTotalTrips = itemView.findViewById(R.id.tvHomeTotalTrips);
            tvTotalFare = itemView.findViewById(R.id.tvHomeTotalFare);
            tvCapacity = itemView.findViewById(R.id.tvHomeCapacity); // Add this
            btnDepartReturn = itemView.findViewById(R.id.btnDepartReturn);
        }
    }
}