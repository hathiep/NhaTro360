package com.example.nhatro360;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.example.nhatro360.R;

public class MainActivity extends AppCompatActivity {

    NavigationBarView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    Toast.makeText(MainActivity.this, "Trang chủ", Toast.LENGTH_SHORT).show();
                    // Xử lý khi nhấn vào home
                    return true;
                } else if (itemId == R.id.nav_search) {
                    Toast.makeText(MainActivity.this, "Tìm kiếm", Toast.LENGTH_SHORT).show();
                    // Xử lý khi nhấn vào search
                    return true;
                } else if (itemId == R.id.nav_post) {
                    Toast.makeText(MainActivity.this, "Bài viết", Toast.LENGTH_SHORT).show();
                    // Xử lý khi nhấn vào post
                    return true;
                } else if (itemId == R.id.nav_account) {
                    Toast.makeText(MainActivity.this, "Tài khoản", Toast.LENGTH_SHORT).show();
                    // Xử lý khi nhấn vào account
                    return true;
                }
                return false;
            }
        });
    }
}