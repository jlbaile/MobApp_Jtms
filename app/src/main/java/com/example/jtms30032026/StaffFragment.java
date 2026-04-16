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

public class StaffFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    private List<StaffModel> staffList;
    private StaffAdapter adapter;
    private RecyclerView rvStaff;

    public StaffFragment() {}

    public static StaffFragment newInstance(String param1, String param2) {
        StaffFragment fragment = new StaffFragment();
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
        return inflater.inflate(R.layout.fragment_staff, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText etStaffFirstName = view.findViewById(R.id.etStaffFirstName);
        EditText etStaffLastName = view.findViewById(R.id.etStaffLastName);
        EditText etStaffUsername = view.findViewById(R.id.etStaffUsername);
        EditText etStaffPassword = view.findViewById(R.id.etStaffPassword);
        Button btnStaffSubmit = view.findViewById(R.id.btnStaffSubmit);
        EditText etFarePrice = view.findViewById(R.id.etFarePrice);
        Button btnUpdateFare = view.findViewById(R.id.btnUpdateFare);
        androidx.appcompat.widget.SearchView svStaff = view.findViewById(R.id.svStaff);
        rvStaff = view.findViewById(R.id.rvStaff);

        // Setup RecyclerView
        staffList = new ArrayList<>();
        adapter = new StaffAdapter(staffList);
        rvStaff.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvStaff.setAdapter(adapter);

        // Fetch data on load
        fetchStaff();

        // Search listener
        svStaff.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
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


        // Load current fare price
        RequestQueue fareQueue = Volley.newRequestQueue(requireContext());
        StringRequest fareRequest = new StringRequest(Request.Method.GET,
                "http://192.168.100.30/crud-android-jtms/fareget.php",
                response -> etFarePrice.setText(response.trim()),
                error -> Log.e("VolleyError", error.toString())
        );
        fareQueue.add(fareRequest);

        // Update fare price
        btnUpdateFare.setOnClickListener(v -> {
            String newFare = etFarePrice.getText().toString().trim();
            if (newFare.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a fare price", Toast.LENGTH_SHORT).show();
                return;
            }
            RequestQueue queue = Volley.newRequestQueue(requireContext());
            StringRequest updateRequest = new StringRequest(Request.Method.POST,
                    "http://192.168.100.30/crud-android-jtms/fareupdate.php",
                    response -> {
                        if (response.trim().equals("success")) {
                            Toast.makeText(requireContext(), "Fare Updated Successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Failed to update fare", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> Log.e("VolleyError", error.toString())
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("fare_price", newFare);
                    return params;
                }
            };
            queue.add(updateRequest);
        });

        // Submit button
        btnStaffSubmit.setOnClickListener(v -> {
            final String staffFname = etStaffFirstName.getText().toString();
            final String staffLname = etStaffLastName.getText().toString();
            final String staffUsername = etStaffUsername.getText().toString();
            final String staffPassword = etStaffPassword.getText().toString();

            RequestQueue queue = Volley.newRequestQueue(requireContext());
            String url = AppConfig.BASE_URL + "staffcreate.php";

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    response -> {
                        if (response.trim().equals("success")) {
                            Toast.makeText(requireContext(), "Staff Added Successfully", Toast.LENGTH_SHORT).show();
                            etStaffFirstName.setText("");
                            etStaffLastName.setText("");
                            etStaffUsername.setText("");
                            etStaffPassword.setText("");
                            fetchStaff(); // Refresh list after adding
                        } else {
                            Toast.makeText(requireContext(), "Failed: " + response, Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> Log.e("VolleyError", error.toString())
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("staff_fname", staffFname);
                    params.put("staff_lname", staffLname);
                    params.put("staff_username", staffUsername);
                    params.put("staff_password", staffPassword);
                    return params;
                }
            };
            queue.add(stringRequest);
        });
    }

    private void fetchStaff() {
        String url = AppConfig.BASE_URL + "staffread.php";
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        staffList.clear();
                        JSONArray jsonArray = new JSONArray(response);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            StaffModel staff = new StaffModel(
                                    obj.getString("staff_id"),
                                    obj.getString("staff_fname"),
                                    obj.getString("staff_lname"),
                                    obj.getString("staff_username"),
                                    obj.getString("staff_password")
                            );
                            staffList.add(staff);
                        }
                        adapter.updateList(staffList);
                    } catch (Exception e) {
                        Log.e("JSONError", e.toString());
                    }
                },
                error -> Log.e("VolleyError", error.toString())
        );
        queue.add(stringRequest);
    }
}