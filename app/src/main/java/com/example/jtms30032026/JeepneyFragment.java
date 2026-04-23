package com.example.jtms30032026;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class JeepneyFragment extends Fragment implements JeepneyAdapter.OnItemClickListener {

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

        EditText etDriverName  = view.findViewById(R.id.etDriverName);
        EditText etPlateNumber = view.findViewById(R.id.etPlateNumber);
        EditText etCapacity    = view.findViewById(R.id.etCapacity);
        Button   btnAddJeepney = view.findViewById(R.id.btnAddJeepney);
        EditText svJeepney     = view.findViewById(R.id.svJeepney);
        rvJeepney              = view.findViewById(R.id.rvJeepney);

        // Setup RecyclerView
        jeepneyList = new ArrayList<>();
        adapter = new JeepneyAdapter(jeepneyList, this);
        rvJeepney.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvJeepney.setAdapter(adapter);

        fetchJeepneys();

        // Search
        svJeepney.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Add Jeepney
        btnAddJeepney.setOnClickListener(v -> {
            final String driverName  = etDriverName.getText().toString().trim();
            final String plateNumber = etPlateNumber.getText().toString().trim();
            final String capacity    = etCapacity.getText().toString().trim();

            if (driverName.isEmpty() || plateNumber.isEmpty() || capacity.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            RequestQueue queue = Volley.newRequestQueue(requireContext());
            StringRequest stringRequest = new StringRequest(Request.Method.POST,
                    AppConfig.BASE_URL + "jeepneycreate.php",
                    response -> {
                        if (response.trim().equals("success")) {
                            Toast.makeText(requireContext(), "Jeepney Added Successfully", Toast.LENGTH_SHORT).show();
                            etDriverName.setText("");
                            etPlateNumber.setText("");
                            etCapacity.setText("");
                            fetchJeepneys();
                        } else {
                            Toast.makeText(requireContext(), "Failed: " + response, Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> Log.e("VolleyError", error.toString())
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("driver_name",  driverName);
                    params.put("plate_number", plateNumber);
                    params.put("capacity",     capacity);
                    return params;
                }
            };
            queue.add(stringRequest);
        });
    }

    // ── Adapter callbacks ────────────────────────────────────────────────────

    @Override
    public void onEditClick(JeepneyModel jeepney) {
        showEditDialog(jeepney);
    }

    @Override
    public void onDeleteClick(JeepneyModel jeepney) {
        showDeleteConfirmation(jeepney);
    }

    // ─── Delete Confirmation ──────────────────────────────────────────────────

    private void showDeleteConfirmation(JeepneyModel jeepney) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Jeepney")
                .setMessage("Are you sure you want to delete " +
                        jeepney.getPlate_number() + " (" + jeepney.getDriver_name() + ")?")
                .setPositiveButton("Delete", (dialog, which) -> deleteJeepney(jeepney.getJeepney_id()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ─── Delete Request ───────────────────────────────────────────────────────

    private void deleteJeepney(String jeepneyId) {
        RequestQueue queue = Volley.newRequestQueue(requireContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                AppConfig.BASE_URL + "jeepneydelete.php",
                response -> {
                    if (response.trim().equals("success")) {
                        Toast.makeText(requireContext(), "Jeepney Deleted", Toast.LENGTH_SHORT).show();
                        fetchJeepneys();
                    } else {
                        Toast.makeText(requireContext(), "Delete Failed: " + response, Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("VolleyError", error.toString());
                    Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("jeepney_id", jeepneyId);
                return params;
            }
        };
        queue.add(stringRequest);
    }

    // ─── Edit Dialog ──────────────────────────────────────────────────────────

    private void showEditDialog(JeepneyModel jeepney) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_edit_jeepney, null);

        EditText etEditDriverName  = dialogView.findViewById(R.id.etEditDriverName);
        EditText etEditPlateNumber = dialogView.findViewById(R.id.etEditPlateNumber);
        EditText etEditCapacity    = dialogView.findViewById(R.id.etEditCapacity);

        etEditDriverName.setText(jeepney.getDriver_name());
        etEditPlateNumber.setText(jeepney.getPlate_number());
        etEditCapacity.setText(jeepney.getCapacity());

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        Button btnCancel = dialogView.findViewById(R.id.btnCancelEdit);
        Button btnSave   = dialogView.findViewById(R.id.btnSaveEdit);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String newDriverName  = etEditDriverName.getText().toString().trim();
            String newPlateNumber = etEditPlateNumber.getText().toString().trim();
            String newCapacity    = etEditCapacity.getText().toString().trim();

            if (newDriverName.isEmpty() || newPlateNumber.isEmpty() || newCapacity.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            updateJeepney(jeepney.getJeepney_id(), newDriverName, newPlateNumber, newCapacity);
            dialog.dismiss();
        });

        dialog.show();
    }

    // ─── Update Request ───────────────────────────────────────────────────────

    private void updateJeepney(String jeepneyId, String driverName, String plateNumber, String capacity) {
        RequestQueue queue = Volley.newRequestQueue(requireContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                AppConfig.BASE_URL + "jeepneyupdate.php",
                response -> {
                    if (response.trim().equals("success")) {
                        Toast.makeText(requireContext(), "Jeepney Updated Successfully", Toast.LENGTH_SHORT).show();
                        fetchJeepneys();
                    } else {
                        Toast.makeText(requireContext(), "Update Failed: " + response, Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("VolleyError", error.toString());
                    Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("jeepney_id",   jeepneyId);
                params.put("driver_name",  driverName);
                params.put("plate_number", plateNumber);
                params.put("capacity",     capacity);
                return params;
            }
        };
        queue.add(stringRequest);
    }

    // ─── Fetch Jeepneys ───────────────────────────────────────────────────────

    private void fetchJeepneys() {
        RequestQueue queue = Volley.newRequestQueue(requireContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                AppConfig.BASE_URL + "jeepneyread.php",
                response -> {
                    try {
                        jeepneyList.clear();
                        JSONArray jsonArray = new JSONArray(response);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            jeepneyList.add(new JeepneyModel(
                                    obj.getString("jeepney_id"),
                                    obj.getString("driver_name"),
                                    obj.getString("plate_number"),
                                    obj.getString("capacity")
                            ));
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