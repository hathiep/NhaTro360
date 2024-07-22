package com.example.nhatro360;

import java.io.File;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.ImageView;
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
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.widget.Toast;

import com.example.nhatro360.models.Address;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CreatePost extends AppCompatActivity {

    private TextView tvCancel, tvNext;
    private List<TextView> listTv;
    private List<ImageView> listImv, listLine;
    private Address address;
    private Room room;
    private CreatPostViewModel viewModel;

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
        viewModel = new ViewModelProvider(this).get(CreatPostViewModel.class);
        room = viewModel.getRoom();
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
                }
            } else if (currentFragment instanceof FragmentInformation) {
                if (validateInformation()) {
                    loadFragment(new FragmentImage(), true);
                    setNextStep(2, 1);
                }
            } else if (currentFragment instanceof FragmentImage) {
                if (!((FragmentImage) currentFragment).getImageList().isEmpty()){
                    room.setImages(((FragmentImage) currentFragment).getImageList());
                    viewModel.setRoom(room);
                    loadFragment(new FragmentConfirm(), true);
                    setNextStep(3, 2);
                }
                else {
                    showError("Vui lòng chọn hình ảnh cho bài đăng!");
                }
            }
            else if (currentFragment instanceof FragmentConfirm) {
                if(validateConfirm()){
                    showConfirmDialog();
                }
            }
        });

        tvCancel.setOnClickListener(v -> {
            Fragment currentFragment = getCurrentFragment();
            if (currentFragment instanceof FragmentAddress) {
                showCancelDialog();
            } else {
                if (currentFragment instanceof FragmentConfirm) {
                    setBackStep(2, 3);
                } else if (currentFragment instanceof FragmentImage) {
                    room.setImages(((FragmentImage) currentFragment).getImageList());
                    viewModel.setRoom(room);
                    setBackStep(1, 2);
                } else if (currentFragment instanceof FragmentInformation) {
                        viewModel.setRoom(((FragmentInformation) getCurrentFragment()).getRoom());
                    setBackStep(0, 1);
                }
                getSupportFragmentManager().popBackStack();
            }
        });

        getSupportFragmentManager().addOnBackStackChangedListener(this::updateButtons);
    }

    private void loadFragment(Fragment fragment, boolean addToBackStack) {
        if (fragment instanceof FragmentImage) {
            ((FragmentImage) fragment).setImageList(room.getImages());
        }
        if (fragment instanceof FragmentImage) {
            ((FragmentImage) fragment).setImageList(room.getImages());
        }
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

    private Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }

    private void saveRoomToFireStoreDatabase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Create a new room with the fields specified
        Map<String, Object> roomData = new HashMap<>();
        roomData.put("address", room.getAddress());
        roomData.put("area", room.getArea());
        roomData.put("detail", room.getDetail());
        roomData.put("host", room.getHost());
        roomData.put("phone", room.getPhone());
        roomData.put("postType", room.getPostType());
        roomData.put("price", formatPrice(room.getPrice()));
        roomData.put("roomType", room.getRoomType());
        roomData.put("timePosted", FieldValue.serverTimestamp());
        roomData.put("title", room.getTitle());
        roomData.put("utilities", room.getUtilities());

        // Add a new document with a generated ID
        db.collection("rooms")
                .add(roomData)
                .addOnSuccessListener(documentReference -> {
                    String documentId = documentReference.getId();
                    // Call the method to upload images and save their URLs
                    uploadImagesAndSaveUrls(documentId, room.getImages());
                })
                .addOnFailureListener(e -> {
                    // Failed to add room
                    Toast.makeText(CreatePost.this, "Error posting room: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void uploadImagesAndSaveUrls(String documentId, List<String> imagePaths) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("rooms/" + documentId);

        List<String> imageUrls = new ArrayList<>();
        for (String imagePath : imagePaths) {
            Uri fileUri = Uri.parse(imagePath); // Chuyển đổi đường dẫn thành Uri
            StorageReference imageRef = storageRef.child(getFileName(fileUri)); // Lấy tên file từ Uri

            imageRef.putFile(fileUri)
                    .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                imageUrls.add(uri.toString());
                                if (imageUrls.size() == imagePaths.size()) {
                                    saveImageUrlsToFirestore(documentId, imageUrls);
                                }
                            }))
                    .addOnFailureListener(e -> {
                        // Failed to upload image
                        Toast.makeText(CreatePost.this, "Error uploading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private String getFileName(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DISPLAY_NAME };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int nameIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
            String fileName = cursor.getString(nameIndex);
            cursor.close();
            return fileName;
        }
        return uri.getLastPathSegment();
    }


    private void saveImageUrlsToFirestore(String documentId, List<String> imageUrls) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("rooms").document(documentId)
                .update("images", imageUrls)
                .addOnSuccessListener(aVoid -> {
                    // Successfully updated room with image URLs
                    Toast.makeText(CreatePost.this, "Room posted successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Failed to update room with image URLs
                    Toast.makeText(CreatePost.this, "Error updating room with images: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setNextStep(int current, int complete) {
        listTv.get(complete).setTextColor(getResources().getColor(R.color.blue2));
        setAnimationIcon(complete, 1, 1);
        new Handler().postDelayed(() -> setAnimationLine(complete, 1), 200);
        new Handler().postDelayed(() -> setAnimationIcon(current, 2, 1), 100);
    }

    private void setBackStep(int current, int inComplete) {
        listTv.get(current).setTextColor(getResources().getColor(R.color.black2));
        setAnimationIcon(inComplete, 3, 2);
        new Handler().postDelayed(() -> setAnimationLine(current, 2), 200);
        new Handler().postDelayed(() -> setAnimationIcon(current, 2, 2), 100);
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
                time_delay = 100;
                imv.setImageResource(R.drawable.icon_completed_step);
            } else { imv.setImageResource(R.drawable.icon_current_step); time_delay = 100; }
        } else {
            time_delay = 100;
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
        }, 100);
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

        if(province == null || province.isEmpty()){
            showError("Vui lòng chọn Tỉnh/TP!");
            return false;
        }
        if(district == null || district.isEmpty()){
            showError("Vui lòng chọn Quận/Huyện!");
            return false;
        }
        if(ward == null || ward.isEmpty()){
            showError("Vui lòng chọn Phường/Xã!");
            return false;
        }
        if(street == null || street.isEmpty()){
            showError("Vui lòng điền Số nhà, tên đường!");
            return false;
        }

        address = new Address(province, district, ward, street);
        room.setAddress(address.getAddress());
        viewModel.setRoom(room);
        return true;
    }

    private boolean validateInformation(){
        FragmentInformation fragmentInformation = (FragmentInformation) getCurrentFragment();
        Room roomInfor = fragmentInformation.getRoom();
        String roomPrice = roomInfor.getPrice();
        String roomArea = roomInfor.getArea();
        if(roomPrice.equals("")){
            showError("Vui lòng nhập giá phòng!");
            return false;
        }
        if(Integer.parseInt(roomPrice)<100000){
            showError("Giá phòng phải trên 100.000 VND!");
            return false;
        }
        if(roomArea.equals("")){
            showError("Vui lòng nhập diện tích phòng!");
            return false;
        }
        if(Integer.parseInt(roomArea)<5){
            showError("Diện tích phòng phải trên 5 m2!");
            return false;
        }
        if(!roomInfor.getUtilities().contains(true)){
            showError("Chọn tối thiểu một tiện ích");
            return false;
        }
        viewModel.setRoom(roomInfor);
        return true;
    }

    private boolean validateConfirm(){
        FragmentConfirm fragmentConfirm = (FragmentConfirm) getCurrentFragment();
        Room roomInfor = fragmentConfirm.getRoom();
        if(roomInfor.getTitle().equals("") || roomInfor.getHost().equals("") ||
                roomInfor.getPhone().equals("") || roomInfor.getDetail().equals("") ){
            showError("Vui lòng nhập đầy đủ thông tin!");
            return false;
        }
        viewModel.setRoom(roomInfor);
        return true;
    }

    private String formatPrice(String price){
        DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
        double millions = Integer.parseInt(price) / 1_000_000.0;
        return decimalFormat.format(millions) + " triệu";
    }

    private void showConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setMessage(R.string.confirm_message)
                .setPositiveButton(R.string.yes, null)
                .setNegativeButton(R.string.no, null);

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.red));
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(true);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setAllCaps(true);

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                saveRoomToFireStoreDatabase();
            });
        });

        dialog.show();
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

    private void showError(String message) {
        Toast.makeText(CreatePost.this, message, Toast.LENGTH_SHORT).show();
//        new AlertDialog.Builder(this)
//                .setMessage(message)
//                .setPositiveButton("OK", null)
//                .show();
    }

}
