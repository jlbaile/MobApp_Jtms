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
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JeepneyFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    private List<JeepneyModel> jeepneyList;
    private JeepneyAdapter adapter;
    private RecyclerView rvJeepney;

    public JeepneyFragment() {}

    public static JeepneyFragment newInstance(String param1, String param2) {
        JeepneyFragment fragment = new JeepneyFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_jeepney, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Init views
        EditText etDriverName = view.findViewById(R.id.etDriverName);
        EditText etPlateNumber = view.findViewById(R.id.etPlateNumber);
        EditText etCapacity = view.findViewById(R.id.etCapacity);
        Button btnAddJeepney = view.findViewById(R.id.btnAddJeepney);
        androidx.appcompat.widget.SearchView svJeepney = view.findViewById(R.id.svJeepney);
        rvJeepney = view.findViewById(R.id.rvJeepney);

        // Setup RecyclerView
        jeepneyList = new ArrayList<>();
        adapter = new JeepneyAdapter(jeepneyList);
        rvJeepney.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvJeepney.setAdapter(adapter);

        // Fetch data on load
        fetchJeepneys();

        // Search listener
        svJeepney.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return false;
            }
        });



        // Add Jeepney button
        btnAddJeepney.setOnClickListener(v -> {
            final String driverName = etDriverName.getText().toString();
            final String plateNumber = etPlateNumber.getText().toString();
            final String capacity = etCapacity.getText().toString();

            RequestQueue queue = Volley.newRequestQueue(requireContext());
            String url = AppConfig.BASE_URL + "jeepneycreate.php";

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    response -> {
                        if (response.trim().equals("success")) {
                            Toast.makeText(requireContext(), "Jeepney Added Successfully", Toast.LENGTH_SHORT).show();
                            etDriverName.setText("");
                            etPlateNumber.setText("");
                            etCapacity.setText("");
                            fetchJeepneys(); // Refresh list after adding
                        } else {
                            Toast.makeText(requireContext(), "Failed: " + response, Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> Log.e("VolleyError", error.toString())
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("driver_name", driverName);
                    params.put("plate_number", plateNumber);
                    params.put("capacity", capacity);
                    return params;
                }
            };
            queue.add(stringRequest);
        });
    }

    private void fetchJeepneys() {
        String url = AppConfig.BASE_URL + "jeepneyread.php";
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        jeepneyList.clear();
                        JSONArray jsonArray = new JSONArray(response);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            JeepneyModel jeepney = new JeepneyModel(
                                    obj.getString("jeepney_id"),
                                    obj.getString("driver_name"),
                                    obj.getString("plate_number"),
                                    obj.getString("capacity")
                            );
                            jeepneyList.add(jeepney);
                        }
                        adapter.updateList(jeepneyList);
                    } catch (Exception e) {
                        Log.e("JSONError", e.toString());
                    }
                },
                error -> Log.e("VolleyError", error.toString())
        );
        queue.add(stringRequest);
    }
}