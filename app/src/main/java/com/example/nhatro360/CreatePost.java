package com.example.nhatro360;

import android.animation.ObjectAnimator;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;


public class CreatePost extends AppCompatActivity {

    private TextView tvCancel, tvNext;
    private List<TextView> listTv;
    private List<ImageView> listImv;

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

        init();

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

    private void init() {
        listImv = new ArrayList<>();
        listTv = new ArrayList<>();
        tvCancel = findViewById(R.id.tv_cancel);
        tvNext = findViewById(R.id.tv_next);
        listTv.add(findViewById(R.id.tv_address));
        listTv.add(findViewById(R.id.tv_information));
        listTv.add(findViewById(R.id.tv_image));
        listTv.add(findViewById(R.id.tv_confirm));
        listImv.add(findViewById(R.id.imv_address));
        listImv.add(findViewById(R.id.imv_information));
        listImv.add(findViewById(R.id.imv_image));
        listImv.add(findViewById(R.id.imv_confirm));
    }

    private void setOnclickHeader() {

        tvNext.setOnClickListener(v -> {
            Fragment currentFragment = getCurrentFragment();
            if (currentFragment instanceof FragmentAddress) {
                loadFragment(new FragmentInformation(), true);
                setNextStep(1, 0);
            } else if (currentFragment instanceof FragmentInformation) {
                loadFragment(new FragmentImage(), true);
                setNextStep(2, 1);
            } else if (currentFragment instanceof FragmentImage) {
                loadFragment(new FragmentConfirm(), true);
                setNextStep(3,2);
            }
        });

        tvCancel.setOnClickListener(v -> {
            Fragment currentFragment = getCurrentFragment();
            if (currentFragment instanceof FragmentAddress) {
                showCancelDialog();
            } else {
                if(currentFragment instanceof FragmentConfirm) {
                    setBackStep(2, 3);
                }
                else if(currentFragment instanceof FragmentImage) {
                    setBackStep(1, 2);
                }
                else if(currentFragment instanceof FragmentInformation) {
                    setBackStep(0, 1);
                }
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

    private void setNextStep(int current, int complete) {
        listTv.get(complete).setTextColor(getResources().getColor(R.color.blue2));
        setAnimation(complete, 1, 1);
        new Handler().postDelayed(() -> setAnimation(current, 2, 1), 200);
    }

    private void setBackStep(int current, int inComplete) {
        listTv.get(current).setTextColor(getResources().getColor(R.color.black2));
        setAnimation(inComplete, 3, 2);
        new Handler().postDelayed(() -> setAnimation(current, 2, 2), 100);
    }
    private void setAnimation(int cnt, int i, int direction){
        int time_delay = 100;
        ImageView imv = listImv.get(cnt);
        float scaleA, scaleB;
        if(direction == 1) {
            scaleA = 0f;
            scaleB = 1f;
            listImv.get(cnt).setImageResource(0);
            if(i == 1) imv.setImageResource(R.drawable.icon_completed_step);
            else imv.setImageResource(R.drawable.icon_current_step);
        } else {
            scaleA = 1f;
            scaleB = 0f;
            if(i == 3) {
                listImv.get(cnt).setImageResource(0);
                imv.setImageResource(R.drawable.icon_current_step);
            }
            else {
                imv.setImageResource(R.drawable.icon_completed_step);
            }
        }
        imv.setScaleX(scaleA);
        imv.setScaleY(scaleA);
        new Handler().postDelayed(() -> {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(imv, "scaleX", scaleA, scaleB);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(imv, "scaleY", scaleA, scaleB);
            scaleX.setDuration(100); // Thời gian hiệu ứng (1 giây)
            scaleY.setDuration(100);
            scaleX.start();
            scaleY.start();
        }, time_delay);
        if(direction == 2 && i == 2) new Handler().postDelayed(() ->  listImv.get(cnt).setImageResource(R.drawable.icon_current_step), 1000);
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
            tvNext.setText(R.string.post);
        } else {
            tvNext.setText(R.string.next);
        }
    }
}
