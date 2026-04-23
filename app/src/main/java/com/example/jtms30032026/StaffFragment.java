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
import android.widget.TextView;
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
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaffFragment extends Fragment implements StaffAdapter.OnItemClickListener {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    private List<StaffModel> staffList;
    private StaffAdapter adapter;
    private RecyclerView rvStaff;
    private TextView tvStaffCount;

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

        // Init views — new IDs from redesigned fragment_staff.xml
        EditText etFirstName      = view.findViewById(R.id.etFirstName);
        EditText etLastName       = view.findViewById(R.id.etLastName);
        EditText etUsername       = view.findViewById(R.id.etUsername);
        TextInputEditText etPassword = view.findViewById(R.id.etPassword);
        Button   btnAddStaff      = view.findViewById(R.id.btnAddStaff);
        EditText etFarePrice      = view.findViewById(R.id.etFarePrice);
        Button   btnUpdateFare    = view.findViewById(R.id.btnUpdateFare);
        EditText svStaff          = view.findViewById(R.id.svStaff); // now a plain EditText
        tvStaffCount              = view.findViewById(R.id.tvStaffCount);
        rvStaff                   = view.findViewById(R.id.rvStaff);

        // Setup RecyclerView
        staffList = new ArrayList<>();
        adapter = new StaffAdapter(staffList, this);
        rvStaff.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvStaff.setAdapter(adapter);

        // Fetch staff on load
        fetchStaff();

        // Search — TextWatcher replaces SearchView.OnQueryTextListener
        svStaff.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Load current fare price
        RequestQueue fareQueue = Volley.newRequestQueue(requireContext());
        StringRequest fareRequest = new StringRequest(Request.Method.GET,
                AppConfig.BASE_URL + "fareget.php",
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
                    AppConfig.BASE_URL + "fareupdate.php",
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

        // Add Staff button
        btnAddStaff.setOnClickListener(v -> {
            final String staffFname    = etFirstName.getText().toString().trim();
            final String staffLname    = etLastName.getText().toString().trim();
            final String staffUsername = etUsername.getText().toString().trim();
            final String staffPassword = etPassword.getText() != null
                    ? etPassword.getText().toString().trim() : "";

            if (staffFname.isEmpty() || staffLname.isEmpty() || staffUsername.isEmpty() || staffPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            RequestQueue queue = Volley.newRequestQueue(requireContext());
            String url = AppConfig.BASE_URL + "staffcreate.php";

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    response -> {
                        if (response.trim().equals("success")) {
                            Toast.makeText(requireContext(), "Staff Added Successfully", Toast.LENGTH_SHORT).show();
                            etFirstName.setText("");
                            etLastName.setText("");
                            etUsername.setText("");
                            etPassword.setText("");
                            fetchStaff();
                        } else {
                            Toast.makeText(requireContext(), "Failed: " + response, Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> Log.e("VolleyError", error.toString())
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("staff_fname",    staffFname);
                    params.put("staff_lname",    staffLname);
                    params.put("staff_username", staffUsername);
                    params.put("staff_password", staffPassword);
                    return params;
                }
            };
            queue.add(stringRequest);
        });
    }

    // Called when ⋮ menu is tapped on a staff item
    @Override
    public void onItemClick(StaffModel staff) {
        showEditDialog(staff);
    }

    // ─── Edit Dialog ──────────────────────────────────────────────────────────

    private void showEditDialog(StaffModel staff) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_edit_staff, null);

        EditText etEditFname             = dialogView.findViewById(R.id.etEditStaffFname);
        EditText etEditLname             = dialogView.findViewById(R.id.etEditStaffLname);
        EditText etEditUsername          = dialogView.findViewById(R.id.etEditStaffUsername);
        TextInputEditText etEditPassword = dialogView.findViewById(R.id.etEditStaffPassword);

        // Pre-fill current values
        etEditFname.setText(staff.getStaff_fname());
        etEditLname.setText(staff.getStaff_lname());
        etEditUsername.setText(staff.getStaff_username());
        etEditPassword.setText(staff.getStaff_password());

        // Build dialog using the new custom layout
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        // Wire layout buttons instead of AlertDialog built-ins
        Button btnCancel = dialogView.findViewById(R.id.btnCancelStaffEdit);
        Button btnSave   = dialogView.findViewById(R.id.btnSaveStaffEdit);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String newFname    = etEditFname.getText().toString().trim();
            String newLname    = etEditLname.getText().toString().trim();
            String newUsername = etEditUsername.getText().toString().trim();
            String newPassword = etEditPassword.getText() != null
                    ? etEditPassword.getText().toString().trim() : "";

            if (newFname.isEmpty() || newLname.isEmpty() || newUsername.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            updateStaff(staff.getStaff_id(), newFname, newLname, newUsername, newPassword);
            dialog.dismiss();
        });

        dialog.show();
    }

    // ─── Update Request ───────────────────────────────────────────────────────

    private void updateStaff(String staffId, String fname, String lname, String username, String password) {
        String url = AppConfig.BASE_URL + "staffupdate.php";
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    if (response.trim().equals("success")) {
                        Toast.makeText(requireContext(), "Staff Updated Successfully", Toast.LENGTH_SHORT).show();
                        fetchStaff();
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
                params.put("staff_id",       staffId);
                params.put("staff_fname",    fname);
                params.put("staff_lname",    lname);
                params.put("staff_username", username);
                params.put("staff_password", password);
                return params;
            }
        };
        queue.add(stringRequest);
    }

    // ─── Fetch Staff ──────────────────────────────────────────────────────────

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
                        // Update the total count badge
                        tvStaffCount.setText(staffList.size() + " Total");
                    } catch (Exception e) {
                        Log.e("JSONError", e.toString());
                    }
                },
                error -> Log.e("VolleyError", error.toString())
        );
        queue.add(stringRequest);
    }
}