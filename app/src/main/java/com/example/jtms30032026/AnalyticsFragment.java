package com.example.jtms30032026;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AnalyticsFragment extends Fragment {

    private static final String URL_ANALYTICS = AppConfig.BASE_URL + "analyticsread.php";
    private TextView tvTripsToday, tvEstPassengers, tvRevenue;
    private EditText etSearch;
    private RecyclerView rvTripLogs;
    private TripLogAdapter tripLogAdapter;
    private List<TripLogItem> allTrips = new ArrayList<>();

    public AnalyticsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analytics, container, false);

        tvTripsToday    = view.findViewById(R.id.tvTripsToday);
        tvEstPassengers = view.findViewById(R.id.tvEstPassengers);
        tvRevenue       = view.findViewById(R.id.tvRevenue);
        etSearch        = view.findViewById(R.id.etSearch);
        rvTripLogs      = view.findViewById(R.id.rvTripLogs);

        tripLogAdapter = new TripLogAdapter();
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvTripLogs.setLayoutManager(llm);
        rvTripLogs.setNestedScrollingEnabled(false);
        rvTripLogs.setHasFixedSize(false);
        rvTripLogs.setAdapter(tripLogAdapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTrips(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadAnalytics();
        return view;
    }

    private void filterTrips(String query) {
        if (allTrips == null || allTrips.isEmpty()) return;

        if (query.trim().isEmpty()) {
            tripLogAdapter.setItems(new ArrayList<>(allTrips));
            return;
        }
        List<TripLogItem> filtered = new ArrayList<>();
        String lower = query.toLowerCase().trim();
        for (TripLogItem item : allTrips) {
            if (item.plateNumber.toLowerCase().contains(lower) ||
                    item.driverName.toLowerCase().contains(lower)) {
                filtered.add(item);
            }
        }
        tripLogAdapter.setItems(filtered);
    }

    private void loadAnalytics() {
        new Thread(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(URL_ANALYTICS).openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                JSONObject json = new JSONObject(sb.toString());
                if (json.getBoolean("success")) {
                    int trips      = json.getInt("trips_today");
                    int passengers = json.getInt("est_passengers");
                    double revenue = json.getDouble("projected_revenue");

                    NumberFormat numFmt = NumberFormat.getNumberInstance(Locale.US);
                    String revenueStr = "₱" + numFmt.format(revenue);

                    JSONArray tripsArray = json.getJSONArray("trips_list");
                    List<TripLogItem> tripList = new ArrayList<>();
                    for (int i = 0; i < tripsArray.length(); i++) {
                        JSONObject t = tripsArray.getJSONObject(i);
                        tripList.add(new TripLogItem(
                                t.getString("plate_number"),
                                t.getString("driver_name"),
                                t.getString("depart_time"),
                                t.getString("return_time"),
                                t.getInt("capacity"),
                                t.getString("fare")
                        ));
                    }
                    allTrips = new ArrayList<>(tripList);

                    requireActivity().runOnUiThread(() -> {
                        tvTripsToday.setText(numFmt.format(trips));
                        tvEstPassengers.setText(numFmt.format(passengers));
                        tvRevenue.setText(revenueStr);
                        tripLogAdapter.setItems(new ArrayList<>(allTrips));
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}