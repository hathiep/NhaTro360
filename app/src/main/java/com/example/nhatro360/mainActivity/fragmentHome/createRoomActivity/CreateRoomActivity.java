package com.example.nhatro360.mainActivity.fragmentHome.createRoomActivity;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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

import com.example.nhatro360.mainActivity.MainActivity;
import com.example.nhatro360.R;
import com.example.nhatro360.model.Notification;
import com.example.nhatro360.model.Room;
import com.example.nhatro360.model.Address;
import com.example.nhatro360.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CreateRoomActivity extends AppCompatActivity {

    private TextView tvCancel, tvNext;
    private List<TextView> listTv;
    private List<ImageView> listImv, listLine;
    private Address address;
    private Room room;
    private CreateRoomViewModel viewModel;
    private ProgressDialog progressDialog;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private static User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_room);

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

        // Hiển thị fragment đầu tiên
        if (savedInstanceState == null) {
            loadFragment(new AddressFragment(), false);
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

    // Hàm ánh xạ view
    private void init() {
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        user = new User();
        viewModel = new ViewModelProvider(this).get(CreateRoomViewModel.class);
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

    // Hàm lấy thông tin tài khoản hiện tại
    private void getCurrentUser(FirestoreCallback callback) {
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            db.collection("users").whereEqualTo("email", userEmail)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            DocumentSnapshot userDoc = task.getResult().getDocuments().get(0);
                            User user = userDoc.toObject(User.class);
                            user.setId(userDoc.getId());
                            callback.onCallback(user);
                        } else {
                            // Xử lý lỗi nếu có
                        }
                    });
        }
    }

    // Hàm bắt sự kiện click nút back và next
    private void setOnclickHeader() {
        // Nút tiếp theo
        tvNext.setOnClickListener(v -> {
            Fragment currentFragment = getCurrentFragment();
            if (currentFragment instanceof AddressFragment) {
                if (validateAddress()) {
                    loadFragment(new InformationFragment(), true);
                    setNextStep(1, 0);
                }
            } else if (currentFragment instanceof InformationFragment) {
                if (validateInformation()) {
                    loadFragment(new ImageFragment(), true);
                    setNextStep(2, 1);
                }
            } else if (currentFragment instanceof ImageFragment) {
                if (!((ImageFragment) currentFragment).getImageList().isEmpty()){
                    // Lưu danh sách ảnh vào viewModel
                    room.setImages(((ImageFragment) currentFragment).getImageList());
                    viewModel.setRoom(room);
                    loadFragment(new ConfirmFragment(), true);
                    setNextStep(3, 2);
                }
                else {
                    showError("Vui lòng chọn hình ảnh cho bài đăng!");
                }
            }
            else if (currentFragment instanceof ConfirmFragment) {
                if(validateConfirm()){
                    showConfirmDialog();
                }
            }
        });

        // Nút Huỷ/Quay lại
        tvCancel.setOnClickListener(v -> {
            Fragment currentFragment = getCurrentFragment();
            if (currentFragment instanceof AddressFragment) {
                // Nếu ở bước điền địa chỉ thì hiển thị thông báo huỷ
                showCancelDialog();
            } else {
                // Nếu ở các bước khác thì quay lại bước trước đó
                // Đồng thời lưu lại trạng thái phòng đang tạo vào ViewModel
                if (currentFragment instanceof ConfirmFragment) {
                    viewModel.setRoom(((ConfirmFragment) getCurrentFragment()).getRoom());
                    setBackStep(2, 3);
                } else if (currentFragment instanceof ImageFragment) {
                    room.setImages(((ImageFragment) currentFragment).getImageList());
                    viewModel.setRoom(room);
                    setBackStep(1, 2);
                } else if (currentFragment instanceof InformationFragment) {
                    viewModel.setRoom(((InformationFragment) getCurrentFragment()).getRoom());
                    setBackStep(0, 1);
                }
                getSupportFragmentManager().popBackStack();
            }
        });

        getSupportFragmentManager().addOnBackStackChangedListener(this::updateButtons);
    }

    // Hàm check dữ liệu trống khi nhập địa chỉ
    private boolean validateAddress() {
        AddressFragment addressFragment = (AddressFragment) getCurrentFragment();

        String province = addressFragment.getProvince();
        String district = addressFragment.getDistrict();
        String ward = addressFragment.getWard();
        String street = addressFragment.getStreet();

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

    // Hàm check dữ liệu trống khi nhập thông tin
    private boolean validateInformation(){
        InformationFragment informationFragment = (InformationFragment) getCurrentFragment();
        Room roomInfor = informationFragment.getRoom();
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

    // Hàm check dữ liệu trống khi ở bước xác nhận
    private boolean validateConfirm(){
        ConfirmFragment confirmFragment = (ConfirmFragment) getCurrentFragment();
        Room roomInfor = confirmFragment.getRoom();
        if(roomInfor.getTitle().equals("") || roomInfor.getHost().equals("") ||
                roomInfor.getPhone().equals("") || roomInfor.getDetail().equals("") ){
            showError("Vui lòng nhập đầy đủ thông tin!");
            return false;
        }
        viewModel.setRoom(roomInfor);
        return true;
    }

    // Hàm tuỳ chỉnh view các bước khi click vào next
    private void setNextStep(int current, int complete) {
        listTv.get(complete).setTextColor(getResources().getColor(R.color.blue2));
        setAnimationIcon(complete, 1, 1);
        new Handler().postDelayed(() -> setAnimationLine(complete, 1), 100);
        new Handler().postDelayed(() -> setAnimationIcon(current, 2, 1), 50);
    }

    // Hàm tuỳ chỉnh view các bước khi click vào back
    private void setBackStep(int current, int inComplete) {
        listTv.get(current).setTextColor(getResources().getColor(R.color.black2));
        setAnimationIcon(inComplete, 3, 2);
        new Handler().postDelayed(() -> setAnimationLine(current, 2), 100);
        new Handler().postDelayed(() -> setAnimationIcon(current, 2, 2), 50);
    }

    // Hàm thay đổi hiệu ứng Icon
    private void setAnimationIcon(int cnt, int i, int direction) {
        int time_delay;
        ImageView imv = listImv.get(cnt);
        float scaleA, scaleB;

        if (direction == 1) {
            scaleA = 0f;
            scaleB = 1f;
            listImv.get(cnt).setImageResource(0);
            if (i == 1) {
                time_delay = 50;
                imv.setImageResource(R.drawable.ic_completed_step);
            } else { imv.setImageResource(R.drawable.ic_current_step); time_delay = 50; }
        } else {
            time_delay = 50;
            scaleA = 1f;
            scaleB = 0f;
            if (i == 3) {
                imv.setImageResource(R.drawable.ic_current_step);
            } else {
                imv.setImageResource(R.drawable.ic_completed_step);
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
                        imv.setImageResource(R.drawable.ic_current_step);
                        ObjectAnimator scaleX = ObjectAnimator.ofFloat(imv, "scaleX", 0f, 1f);
                        ObjectAnimator scaleY = ObjectAnimator.ofFloat(imv, "scaleY", 0f, 1f);
                        scaleX.setDuration(50);
                        scaleY.setDuration(50);
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
        }, 50);
    }

    // Hàm thay đổi hiệu ứng đường thẳng
    private void setAnimationLine(int i, int direction) {
        if(direction == 2) {
            listLine.get(i).setBackgroundResource(R.drawable.ic_line);
            listLine.get(i+3).setBackgroundResource(R.drawable.ic_line);
            return;
        }
        listLine.get(i).setBackgroundResource(R.drawable.ic_line2);
        listLine.get(i+3).setBackgroundResource(R.drawable.ic_line2);
    }

    // Hàm cập nhật tên của nút back và next
    private void updateButtons() {
        if (tvCancel == null || tvNext == null) {
            return; // Tránh lỗi NullPointerException
        }

        Fragment currentFragment = getCurrentFragment();
        if (currentFragment instanceof AddressFragment) {
            tvCancel.setText(R.string.cancel);
            tvCancel.setTextColor(getResources().getColor(R.color.red));
        } else {
            tvCancel.setText(R.string.back);
            tvCancel.setTextColor(getResources().getColor(R.color.black));
        }

        if (currentFragment instanceof ConfirmFragment) {
            tvNext.setText(R.string.post);
        } else {
            tvNext.setText(R.string.next);
        }
    }

    // Hàm Hàm hiển thị bước hiện tại
    private void loadFragment(Fragment fragment, boolean addToBackStack) {
        if (fragment instanceof ImageFragment) {
            ((ImageFragment) fragment).setImageList(room.getImages());
        }
        if (fragment instanceof ImageFragment) {
            ((ImageFragment) fragment).setImageList(room.getImages());
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

    // Hàm hiển thị thông báo xác nhận tạo phòng
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
                // Gọi hàm lưu phòng vào firestore
                saveRoomToFireStoreDatabase();
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    // Hàm lưu phòng hiện tại lên Firestore
    private void saveRoomToFireStoreDatabase() {
        showProgressDialog("Đang lưu dữ liệu...");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Create a new room with the fields specified
        Map<String, Object> roomData = new HashMap<>();
        roomData.put("address", room.getAddress());
        roomData.put("area", room.getArea());
        roomData.put("avatar", room.getAvatar());
        roomData.put("detail", room.getDetail());
        roomData.put("host", room.getHost());
        roomData.put("phone", room.getPhone());
        roomData.put("postType", room.getPostType() + 1);
        roomData.put("price", room.getPrice());
        roomData.put("roomType", room.getRoomType() + 1);
        roomData.put("status", 0);
        roomData.put("timePosted", FieldValue.serverTimestamp());
        roomData.put("title", room.getTitle());
        roomData.put("utilities", room.getUtilities());

        // Add a new document with a generated ID
        db.collection("rooms")
                .add(roomData)
                .addOnSuccessListener(documentReference -> {
                    String documentId = documentReference.getId();
                    getCurrentUser(new FirestoreCallback() {
                        @Override
                        public void onCallback(User userData) {
                            user = userData;
                            user.getPostedRooms().add(documentId);
                            // Gọi hàm cập nhật danh sách phòng đã đăng của người dùng
                            updateUser(documentId);
                        }
                    });
                    // Gọi hàm lưu ảnh và tạo đường dẫn
                    uploadImagesAndSaveUrls(documentId, room.getImages());
                })
                .addOnFailureListener(e -> {
                    // Failed to add room
                    progressDialog.dismiss();
                    Toast.makeText(CreateRoomActivity.this, "Error posting room: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Hàm cập nhật danh sách phòng đã đăng của người dùng
    private void updateUser(String roomId){
        DocumentReference userRef = db.collection("users").document(user.getId());
        // Tạo đối tượng Notification mới
        Notification newNotification = new Notification(roomId, Timestamp.now(), 1);
        List<Notification> notifications = user.getNotifications();
        notifications.add(newNotification);

        // Cập nhật trường notifications trong Firestore
        userRef.update("notifications", notifications)
                .addOnSuccessListener(aVoid -> {
                    // Cập nhật trường postedRooms với danh sách toàn cục
                    userRef.update("postedRooms", user.getPostedRooms())
                            .addOnSuccessListener(aVoid1 -> {
                                Log.d("Firestore", "User attribute updated successfully.");
                            })
                            .addOnFailureListener(e -> {
                                Log.w("Firestore", "Error updating postedRooms.", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.w("Firestore", "Error updating notifications.", e);
                });
    }

    // Hàm lưu ảnh và tạo đường dẫn
    private void uploadImagesAndSaveUrls(String documentId, List<String> imagePaths) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("rooms/" + documentId);

        ConcurrentHashMap<Integer, String> imageUrlsMap = new ConcurrentHashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(4); // Số lượng tiến trình song song, có thể tùy chỉnh
        for (int i = 0; i < imagePaths.size(); i++) {
            String imagePath = imagePaths.get(i);
            int index = i;
            executor.execute(() -> {
                try {
                    Uri fileUri = Uri.parse(imagePath); // Chuyển đổi đường dẫn thành Uri
                    StorageReference imageRef = storageRef.child("image_" + index + ".jpg"); // Đặt tên tệp theo thứ tự

                    imageRef.putFile(fileUri)
                            .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                                    .addOnSuccessListener(uri -> {
                                        imageUrlsMap.put(index, uri.toString());
                                        if (imageUrlsMap.size() == imagePaths.size()) {
                                            List<String> sortedImageUrls = new ArrayList<>(imagePaths.size());
                                            for (int j = 0; j < imagePaths.size(); j++) {
                                                sortedImageUrls.add(imageUrlsMap.get(j));
                                            }
                                            // Gọi hàm lưu ảnh vào storage
                                            saveImageUrlsToFirestore(documentId, sortedImageUrls);
                                        }
                                    }))
                            .addOnFailureListener(e -> {
                                // Failed to upload image
                                runOnUiThread(() -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(CreateRoomActivity.this, "Error uploading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                            });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(CreateRoomActivity.this, "Error uploading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }
        executor.shutdown(); // Đảm bảo tất cả các tiến trình đều hoàn tất
    }

    // Hàm lưu ảnh vào storage
    private void saveImageUrlsToFirestore(String documentId, List<String> imageUrls) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("rooms").document(documentId)
                .update("images", imageUrls)
                .addOnSuccessListener(aVoid -> {
                    // Hiển thị thông báo tạo phòng thành công
                    progressDialog.setMessage("Hoàn tất tạo mới phòng. Vui lòng chờ được chờ xét duyệt.");
                    new Handler().postDelayed(() -> {
                        progressDialog.dismiss();
                        // Chuyển hướng đến trang chủ
                        Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        finish();
                    }, 2000); // Chờ 2 giây trước khi chuyển sang MainActivity
                })
                .addOnFailureListener(e -> {
                    // Failed to update room with image URLs
                    progressDialog.dismiss();
                    Toast.makeText(CreateRoomActivity.this, "Error updating room with images: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Hàm hiển thị thông báo
    private void showProgressDialog(String message) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    // Hàm hiển thị thông báo huỷ đăng tin
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
                dialog.dismiss();
                finish();
                overridePendingTransition(0, 0);
            });
        });

        dialog.show();
    }

    private void showError(String message) {
        Toast.makeText(CreateRoomActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private interface FirestoreCallback {
        void onCallback(User user);
    }

    @Override
    public void onBackPressed(){
        // Gọi hàm hiển thị thông báo huỷ đăng tin khi ấn thoát
        showCancelDialog();
    }
}
