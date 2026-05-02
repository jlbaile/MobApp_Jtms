package com.example.jtms30032026;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements HomeAdapter.OnTripActionListener {

    private List<HomeModel> homeList;
    private HomeAdapter adapter;
    private RecyclerView rvHome;

    // ── Auto-refresh every 5 seconds ──────────────────────────────────────────
    private Handler autoRefreshHandler;
    private Runnable autoRefreshRunnable;
    private static final int REFRESH_INTERVAL_MS = 5000;

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvHome   = view.findViewById(R.id.rvHome);
        homeList = new ArrayList<>();
        adapter  = new HomeAdapter(homeList, this);
        rvHome.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvHome.setAdapter(adapter);

        fetchHome();
        startAutoRefresh();
    }

    // ── Start polling ─────────────────────────────────────────────────────────
    private void startAutoRefresh() {
        autoRefreshHandler  = new Handler(Looper.getMainLooper());
        autoRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                if (isAdded() && !isDetached()) {
                    fetchHome();
                }
                autoRefreshHandler.postDelayed(this, REFRESH_INTERVAL_MS);
            }
        };
        autoRefreshHandler.postDelayed(autoRefreshRunnable, REFRESH_INTERVAL_MS);
    }

    // ── Pause polling when fragment goes off-screen ───────────────────────────
    @Override
    public void onPause() {
        super.onPause();
        if (autoRefreshHandler != null) {
            autoRefreshHandler.removeCallbacks(autoRefreshRunnable);
        }
    }

    // ── Resume polling when fragment comes back on-screen ─────────────────────
    @Override
    public void onResume() {
        super.onResume();
        fetchHome(); // immediate fetch on resume
        if (autoRefreshHandler != null) {
            autoRefreshHandler.postDelayed(autoRefreshRunnable, REFRESH_INTERVAL_MS);
        }
    }

    // ── Stop polling entirely when fragment is destroyed ──────────────────────
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (autoRefreshHandler != null) {
            autoRefreshHandler.removeCallbacks(autoRefreshRunnable);
        }
    }

    // ── Called by adapter after a depart/return action for immediate refresh ──
    @Override
    public void onRefreshNeeded() {
        fetchHome();
    }

    private void fetchHome() {
        String url = AppConfig.BASE_URL + "homeread.php";
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        homeList.clear();
                        JSONArray jsonArray = new JSONArray(response);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            homeList.add(new HomeModel(
                                    obj.getString("jeepney_id"),
                                    obj.getString("driver_name"),
                                    obj.getString("plate_number"),
                                    obj.getString("capacity"),
                                    obj.getString("status"),
                                    obj.getString("last_activity"),
                                    obj.getString("total_trips"),
                                    obj.getString("total_fare"),
                                    obj.optString("active_trip_id", ""),
                                    obj.optString("departed_by", "")
                            ));
                        }
                        adapter.updateList(homeList);
                    } catch (Exception e) {
                        Log.e("JSONError", e.toString());
                    }
                },
                error -> Log.e("VolleyError", error.toString())
        );
        queue.add(stringRequest);
    }
}