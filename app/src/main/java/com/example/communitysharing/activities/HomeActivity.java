package com.example.communitysharing.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.communitysharing.R;
import com.example.communitysharing.fragments.ChatFragment;
import com.example.communitysharing.fragments.HistoryFragment;
import com.example.communitysharing.fragments.HomeFragment;
import com.example.communitysharing.fragments.ProfileFragment;
import com.example.communitysharing.fragments.ShareFragment;
import com.example.communitysharing.utils.LocaleManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleManager.applySavedLocale(this);
        setContentView(R.layout.activity_home);

        bottomNav = findViewById(R.id.bottomNavigation);

        // Mặc định mở HomeFragment
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            bottomNav.setSelectedItemId(R.id.nav_home);
        }

        bottomNav.setOnNavigationItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (id == R.id.nav_activity) {
                fragment = new HistoryFragment();
            } else if (id == R.id.nav_share) {
                fragment = new ShareFragment();
            } else if (id == R.id.nav_chat) {
                fragment = new ChatFragment();
            } else if (id == R.id.nav_profile) {
                fragment = new ProfileFragment();
            }

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }

    public void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
