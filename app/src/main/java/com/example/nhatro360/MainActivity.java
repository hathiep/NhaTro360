package com.example.nhatro360;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private CustomViewPager viewPager; // Use CustomViewPager

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            final WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.setSystemBarsAppearance(
                        0,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                );
            }
            getWindow().setStatusBarColor(getColor(R.color.blue2));
            getWindow().setNavigationBarColor(getColor(R.color.blue2));
        } else {
            // For API < 30
            int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            getWindow().getDecorView().setSystemUiVisibility(flags);
            getWindow().setStatusBarColor(getResources().getColor(R.color.blue2));
            getWindow().setNavigationBarColor(getResources().getColor(R.color.blue2));
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        init();
        setUpViewPager();
        setOnMenuSelected();

        // Thêm fragment mặc định (nếu có)
        if (savedInstanceState == null) {
            openFragment(new HomeFragment());
        }
    }

    public void init(){
        viewPager = findViewById(R.id.view_pager); // Cast to CustomViewPager is not necessary
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void setUpViewPager(){
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        viewPager.setAdapter(viewPagerAdapter);
    }

    private void setOnMenuSelected(){
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    viewPager.setCurrentItem(0, false); // Disable smooth scrolling
                    return true;
                }
                if (itemId == R.id.nav_search) {
                    viewPager.setCurrentItem(1, false); // Disable smooth scrolling
                    return true;
                }
                if (itemId == R.id.nav_post) {
                    viewPager.setCurrentItem(2, false); // Disable smooth scrolling
                    return true;
                }
                if (itemId == R.id.nav_account) {
                    viewPager.setCurrentItem(3, false); // Disable smooth scrolling
                    return true;
                }
                return false;
            }
        });
    }

    public void openFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
