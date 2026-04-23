package com.example.jtms30032026;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

public class LoginFragment extends Fragment {

    public LoginFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Updated IDs to match new fragment_login.xml
        EditText etLoginUsername         = view.findViewById(R.id.etUsername);
        TextInputEditText etLoginPassword = view.findViewById(R.id.etPassword);
        Button btnLogin                  = view.findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            String username = etLoginUsername.getText().toString().trim();
            String password = etLoginPassword.getText() != null
                    ? etLoginPassword.getText().toString().trim() : "";

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check admin credentials first
            if (username.equals("admin") && password.equals("admin123")) {
                goToHome(true); // Admin login
                return;
            }

            // Check staff credentials from database
            RequestQueue queue = Volley.newRequestQueue(requireContext());
            String url = AppConfig.BASE_URL + "logincheck.php";

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    response -> {
                        if (response.trim().equals("success")) {
                            goToHome(false); // Staff login — no access to Staff menu
                        } else if (response.trim().equals("invalid")) {
                            Toast.makeText(requireContext(), "Invalid username or password", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Error: " + response, Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> Log.e("VolleyError", error.toString())
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("staff_username", username);
                    params.put("staff_password", password);
                    return params;
                }
            };
            queue.add(stringRequest);
        });
    }

    private void goToHome(boolean isAdmin) {
        Toast.makeText(requireContext(), "Login Successful!", Toast.LENGTH_SHORT).show();

        // Pass isAdmin so MainActivity knows who logged in
        ((MainActivity) requireActivity()).showBottomNav(isAdmin);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainerView, new HomeFragment())
                .commit();
    }
}