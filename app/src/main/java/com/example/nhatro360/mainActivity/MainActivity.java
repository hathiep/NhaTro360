package com.example.nhatro360.mainActivity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsetsController;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.nhatro360.mainActivity.fragmentAccount.AppInformationFragment;
import com.example.nhatro360.mainActivity.fragmentAccount.EditProfileFragment;
import com.example.nhatro360.mainActivity.fragmentAccount.PostedRoomsFragment;
import com.example.nhatro360.mainActivity.fragmentAccount.SavedRoomsFragment;
import com.example.nhatro360.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private MainViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Đổi theme
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

        FirebaseMessaging.getInstance().subscribeToTopic("NhaTroNotification")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Done";
                        if(task.isSuccessful()){
                            msg = "Failed";
                        }
                    }
                });
    }

    public void init(){
        viewPager = findViewById(R.id.view_pager);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void setUpViewPager(){
        MainViewPagerAdapter mainViewPagerAdapter = new MainViewPagerAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        viewPager.setAdapter(mainViewPagerAdapter);
    }

    // Bắt sự kiện click menu
    private void setOnMenuSelected() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                viewPager.setCurrentItem(0, false);
                return true;
            } else if (itemId == R.id.nav_search) {
                viewPager.setCurrentItem(1, false);
                return true;
            } else if (itemId == R.id.nav_notification) {
                viewPager.setCurrentItem(2, false);
                return true;
            } else if (itemId == R.id.nav_account) {
                viewPager.setCurrentItem(3, false);
                return true;
            }
            return false;
        });
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
            if (currentFragment instanceof AppInformationFragment
                    || currentFragment instanceof EditProfileFragment
                    || currentFragment instanceof PostedRoomsFragment
                    || currentFragment instanceof SavedRoomsFragment) {
                fragmentManager.popBackStack();
            } else {
                showExitConfirmationDialog();
            }
        } else {
            showExitConfirmationDialog();
        }
    }

    private void showExitConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setMessage(R.string.cancel_app)
                .setPositiveButton(R.string.yes, (dialog, which) -> finish())
                .setNegativeButton(R.string.no, null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.red));
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(true);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setAllCaps(true);
        });
        dialog.show();
    }


}
