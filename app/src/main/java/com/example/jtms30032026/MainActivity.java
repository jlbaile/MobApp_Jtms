package com.example.jtms30032026;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.jtms30032026.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Hide bottom nav and show login first
        binding.bottomNavigationView.setVisibility(android.view.View.GONE);
        replaceFragment(new LoginFragment());

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.home) {
                replaceFragment(new HomeFragment());
            } else if (id == R.id.analytics) {
                replaceFragment(new AnalyticsFragment());
            } else if (id == R.id.report) {
                replaceFragment(new ReportFragment());
            } else if (id == R.id.staff) {
                replaceFragment(new StaffFragment());
            } else if (id == R.id.jeepney) {
                replaceFragment(new JeepneyFragment());
            }

            return true;
        });
    }

    public void showBottomNav(boolean adminLoggedIn) {
        this.isAdmin = adminLoggedIn;
        binding.bottomNavigationView.setVisibility(android.view.View.VISIBLE);

        // Hide Staff menu item if logged in as staff
        MenuItem staffMenuItem = binding.bottomNavigationView.getMenu().findItem(R.id.staff);
        staffMenuItem.setVisible(adminLoggedIn);
    }

    public void showBottomNav() {
        showBottomNav(false);
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainerView, fragment);
        fragmentTransaction.commit();
    }
}