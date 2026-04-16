package com.example.jtms30032026;

import android.os.Bundle;

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

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvHome = view.findViewById(R.id.rvHome);
        homeList = new ArrayList<>();
        adapter = new HomeAdapter(homeList, this); // Pass 'this' as listener
        rvHome.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvHome.setAdapter(adapter);

        fetchJeepneyStatus();
    }

    // This gets called by HomeAdapter when Depart or Return is clicked
    @Override
    public void onRefreshNeeded() {
        fetchJeepneyStatus();
    }



    private void fetchJeepneyStatus() {
        String url = AppConfig.BASE_URL + "homeread.php";
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        homeList.clear();
                        JSONArray jsonArray = new JSONArray(response);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            HomeModel jeepney = new HomeModel(
                                    obj.getString("jeepney_id"),
                                    obj.getString("driver_name"),
                                    obj.getString("plate_number"),
                                    obj.getString("capacity"),   // Add this
                                    obj.getString("status"),
                                    obj.getString("last_activity"),
                                    obj.getString("total_trips"),
                                    obj.getString("active_trip_id"),
                                    obj.has("total_fare") ? obj.getString("total_fare") : "0.00"
                            );
                            homeList.add(jeepney);
                        }
                        adapter.updateList(new ArrayList<>(homeList));
                    } catch (Exception e) {
                        Log.e("JSONError", e.toString());
                    }
                },
                error -> Log.e("VolleyError", error.toString())
        );
        queue.add(stringRequest);
    }
}