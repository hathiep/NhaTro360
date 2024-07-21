package com.example.nhatro360;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsetsController;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.os.Handler;

import com.example.nhatro360.models.Address;

import java.util.ArrayList;
import java.util.List;


public class CreatePost extends AppCompatActivity {

    private TextView tvCancel, tvNext;
    private List<TextView> listTv;
    private List<ImageView> listImv, listLine;
    private Address address;

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
        listLine = new ArrayList<>();
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
        listLine.add(findViewById(R.id.line11));
        listLine.add(findViewById(R.id.line21));
        listLine.add(findViewById(R.id.line31));
        listLine.add(findViewById(R.id.line12));
        listLine.add(findViewById(R.id.line22));
        listLine.add(findViewById(R.id.line32));
    }

    private void setOnclickHeader() {

        tvNext.setOnClickListener(v -> {
            Fragment currentFragment = getCurrentFragment();
            if (currentFragment instanceof FragmentAddress) {
                if (validateAddress()) {
                    loadFragment(new FragmentInformation(), true);
                    setNextStep(1, 0);
                } else {
                    showErrorDialog("Vui lòng điền đầy đủ thông tin địa chỉ");
                }
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
        setAnimationIcon(complete, 1, 1);
        new Handler().postDelayed(() -> setAnimationLine(complete, 1), 400);
        new Handler().postDelayed(() -> setAnimationIcon(current, 2, 1), 200);
    }

    private void setBackStep(int current, int inComplete) {
        listTv.get(current).setTextColor(getResources().getColor(R.color.black2));
        setAnimationIcon(inComplete, 3, 2);
        new Handler().postDelayed(() -> setAnimationLine(current, 2), 400);
        new Handler().postDelayed(() -> setAnimationIcon(current, 2, 2), 200);
    }

    private void setAnimationIcon(int cnt, int i, int direction) {
        int time_delay;
        ImageView imv = listImv.get(cnt);
        float scaleA, scaleB;

        if (direction == 1) {
            scaleA = 0f;
            scaleB = 1f;
            listImv.get(cnt).setImageResource(0);
            if (i == 1) {
                time_delay = 200;
                imv.setImageResource(R.drawable.icon_completed_step);
            } else { imv.setImageResource(R.drawable.icon_current_step); time_delay = 100; }
        } else {
            time_delay = 200;
            scaleA = 1f;
            scaleB = 0f;
            if (i == 3) {
                imv.setImageResource(R.drawable.icon_current_step);
            } else {
                imv.setImageResource(R.drawable.icon_completed_step);
            }
        }

        imv.setScaleX(scaleA);
        imv.setScaleY(scaleA);

        new Handler().postDelayed(() -> {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(imv, "scaleX", scaleA, scaleB);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(imv, "scaleY", scaleA, scaleB);
            scaleX.setDuration(time_delay);
            scaleY.setDuration(time_delay);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(scaleX, scaleY);

            if (direction == 2 && i == 2) {
                animatorSet.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {}

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        imv.setImageResource(R.drawable.icon_current_step);
                        ObjectAnimator scaleX = ObjectAnimator.ofFloat(imv, "scaleX", 0f, 1f);
                        ObjectAnimator scaleY = ObjectAnimator.ofFloat(imv, "scaleY", 0f, 1f);
                        scaleX.setDuration(100);
                        scaleY.setDuration(100);
                        AnimatorSet animatorSet2 = new AnimatorSet();
                        animatorSet2.playTogether(scaleX, scaleY);
                        animatorSet2.start();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {}

                    @Override
                    public void onAnimationRepeat(Animator animation) {}
                });
            }

            animatorSet.start();
        }, 200);
    }

    private void setAnimationLine(int i, int direction) {
        if(direction == 2) {
            listLine.get(i).setBackgroundResource(R.drawable.icon_line);
            listLine.get(i+3).setBackgroundResource(R.drawable.icon_line);
            return;
        }
        listLine.get(i).setBackgroundResource(R.drawable.icon_line2);
        listLine.get(i+3).setBackgroundResource(R.drawable.icon_line2);
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

    private boolean validateAddress() {
        FragmentAddress fragmentAddress = (FragmentAddress) getCurrentFragment();

        String province = fragmentAddress.getProvince();
        String district = fragmentAddress.getDistrict();
        String ward = fragmentAddress.getWard();
        String street = fragmentAddress.getStreet();

        // Kiểm tra thông tin địa chỉ hợp lệ
        if (province != null && !province.isEmpty() &&
                district != null && !district.isEmpty() &&
                ward != null && !ward.isEmpty() &&
                street != null && !street.isEmpty()) {

            // Tạo đối tượng Address
            address = new Address(province, district, ward, street);
            return true;
        } else {
            return false;
        }
    }

    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }


}
