package com.example.jtms30032026;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private TextView tvProfileName, tvAvatarInitials, tvProfileRole;
    private TextView tvStaffId, tvProfileUsername, tvProfileRoleBadge;
    private Button btnChangePassword, btnLogOut;

    private int currentStaffId = -1;
    private String currentUsername = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvProfileName       = view.findViewById(R.id.tvProfileName);
        tvAvatarInitials    = view.findViewById(R.id.tvAvatarInitials);
        tvProfileRole       = view.findViewById(R.id.tvProfileRole);
        tvStaffId           = view.findViewById(R.id.tvStaffId);
        tvProfileUsername   = view.findViewById(R.id.tvProfileUsername);
        tvProfileRoleBadge  = view.findViewById(R.id.tvProfileRoleBadge);
        btnChangePassword   = view.findViewById(R.id.btnChangePassword);
        btnLogOut           = view.findViewById(R.id.btnLogOut);

        loadProfile();

        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        btnLogOut.setOnClickListener(v -> showLogOutConfirmDialog());

        return view;
    }

    // ─── Load Profile from PHP ───────────────────────────────────────────────

    private void loadProfile() {
        SessionManager session = SessionManager.getInstance();
        currentUsername = session.getLoggedInUsername();

        // DEBUG — remove after fixing
        //Toast.makeText(getContext(), "Username from session: '" + currentUsername + "'", Toast.LENGTH_LONG).show();

        // Admin is hardcoded — not in jeepney_staff table
        if (currentUsername.equalsIgnoreCase("admin")) {
            displayAdminProfile();
            return;
        }

        String url = AppConfig.BASE_URL + "profileread.php?username=" + currentUsername;

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    // DEBUG — shows raw PHP response
                    //Toast.makeText(getContext(), "Response: " + response, Toast.LENGTH_LONG).show();
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getBoolean("success")) {
                            currentStaffId = obj.getInt("staff_id");
                            String fname    = obj.getString("fname");
                            String lname    = obj.getString("lname");
                            String username = obj.getString("username");

                            String fullName = fname + " " + lname;
                            String initials = getInitials(fname, lname);
                            String role     = session.isAdmin() ? "Administrator" : "Operations Staff";

                            tvProfileName.setText(fullName);
                            tvAvatarInitials.setText(initials);
                            tvProfileRole.setText(role);
                            tvStaffId.setText("ID-" + currentStaffId);
                            tvProfileUsername.setText(username);
                            tvProfileRoleBadge.setText(role);

                        } else {
                            Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Network error loading profile", Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }

    private void displayAdminProfile() {
        tvProfileName.setText("Administrator");
        tvAvatarInitials.setText("AD");
        tvProfileRole.setText("System Administrator");
        tvStaffId.setText("ADMIN");
        tvProfileUsername.setText("admin");
        tvProfileRoleBadge.setText("Administrator");
        // Disable Change Password for admin (no DB record)
        btnChangePassword.setEnabled(false);
        btnChangePassword.setAlpha(0.4f);
    }

    // ─── Change Password Dialog ───────────────────────────────────────────────

    private void showChangePasswordDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_change_password);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        TextInputEditText etCurrent = dialog.findViewById(R.id.etCurrentPassword);
        TextInputEditText etNew     = dialog.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirm = dialog.findViewById(R.id.etConfirmPassword);
        Button btnCancel  = dialog.findViewById(R.id.btnCancelChangePassword);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirmChangePassword);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String current = etCurrent.getText() != null ? etCurrent.getText().toString().trim() : "";
            String newPass  = etNew.getText()     != null ? etNew.getText().toString().trim()     : "";
            String confirm  = etConfirm.getText() != null ? etConfirm.getText().toString().trim() : "";

            if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPass.equals(confirm)) {
                Toast.makeText(getContext(), "New passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPass.length() < 6) {
                Toast.makeText(getContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            changePassword(current, newPass, dialog);
        });

        dialog.show();
    }

    private void changePassword(String currentPass, String newPass, Dialog dialog) {
        String url = AppConfig.BASE_URL + "profilepassword.php";

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getBoolean("success")) {
                            Toast.makeText(getContext(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(getContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("staff_id",         String.valueOf(currentStaffId));
                params.put("current_password",  currentPass);
                params.put("new_password",      newPass);
                return params;
            }
        };

        queue.add(request);
    }

    // ─── Log Out Confirm Dialog ───────────────────────────────────────────────

    private void showLogOutConfirmDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_confirm_logout);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.88),
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        Button btnCancel  = dialog.findViewById(R.id.btnCancelLogout);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirmLogout);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            performLogOut();
        });

        dialog.show();
    }

    private void performLogOut() {
        SessionManager.getInstance().clear();
        if (getActivity() instanceof MainActivity) {
            MainActivity main = (MainActivity) getActivity();
            // Hide bottom nav
            main.binding.bottomNavigationView.setVisibility(android.view.View.GONE);
            // Reset selected tab to home for next login
            main.binding.bottomNavigationView.getMenu().findItem(R.id.home).setChecked(true);
        }
        // Navigate back to LoginFragment
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainerView, new LoginFragment())
                .commit();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private String getInitials(String fname, String lname) {
        String f = (fname != null && !fname.isEmpty()) ? String.valueOf(fname.charAt(0)).toUpperCase() : "";
        String l = (lname != null && !lname.isEmpty()) ? String.valueOf(lname.charAt(0)).toUpperCase() : "";
        return f + l;
    }
}