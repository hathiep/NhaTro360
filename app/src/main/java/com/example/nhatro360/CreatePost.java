package com.example.nhatro360;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class CreatePost extends AppCompatActivity {

    private TextView tvCancel, tvNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_post);

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

        tvCancel = findViewById(R.id.tv_cancel);
        tvNext = findViewById(R.id.tv_next);

        if (savedInstanceState == null) {
            loadFragment(new FragmentAddress(), false);
        } else {
            updateButtons(); // Cập nhật nút khi Activity được tái tạo
        }
        setOnclickHeader();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateButtons();
    }

    private void setOnclickHeader() {

        tvNext.setOnClickListener(v -> {
            Fragment currentFragment = getCurrentFragment();
            if (currentFragment instanceof FragmentAddress) {
                loadFragment(new FragmentInformation(), true);
            } else if (currentFragment instanceof FragmentInformation) {
                loadFragment(new FragmentImage(), true);
            } else if (currentFragment instanceof FragmentImage) {
                loadFragment(new FragmentConfirm(), true);
            }
        });

        tvCancel.setOnClickListener(v -> {
            Fragment currentFragment = getCurrentFragment();
            if (currentFragment instanceof FragmentAddress) {
                showCancelDialog();
            } else {
                getSupportFragmentManager().popBackStack();
            }
        });

        getSupportFragmentManager().addOnBackStackChangedListener(this::updateButtons);

    }

    private Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }

    private void loadFragment(Fragment fragment, boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.commit();
        fragmentManager.executePendingTransactions(); // Đảm bảo transaction hoàn tất
        updateButtons(); // Cập nhật nút sau khi fragment thay đổi
    }

    private void showCancelDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setMessage(R.string.cancel_post_message)
                .setPositiveButton(R.string.yes, null)
                .setNegativeButton(R.string.no, null);

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.red));
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(true);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setAllCaps(true);

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                finish();
            });
        });

        dialog.show();
    }


    private void updateButtons() {
        if (tvCancel == null || tvNext == null) {
            return; // Tránh lỗi NullPointerException
        }

        Fragment currentFragment = getCurrentFragment();
        if (currentFragment instanceof FragmentAddress) {
            tvCancel.setText(R.string.cancel);
            tvCancel.setTextColor(getResources().getColor(R.color.red));
        } else {
            tvCancel.setText(R.string.back);
            tvCancel.setTextColor(getResources().getColor(R.color.black));
        }

        if (currentFragment instanceof FragmentConfirm) {
            tvNext.setText(R.string.confirm);
        } else {
            tvNext.setText(R.string.next);
        }
    }
}
