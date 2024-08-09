package com.example.nhatro360.mainActivity.fragmentHome.createRoomActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhatro360.R;
import com.example.nhatro360.model.Room;

import java.util.ArrayList;
import java.util.List;

public class ImageFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int STORAGE_PERMISSION_CODE = 123;

    private RelativeLayout btnAddImage;
    private TextView tvNumber;
    private RecyclerView rcv;
    private ImageUploadAdapter adapter;
    private TextView tvEmptyMessage, tvSelectImage;
    private List<String> listImage;
    private Room room;
    private CreateRoomViewModel viewModel;
    private int position = -1; // Vị trí của ảnh đại diện


    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image, container, false);

        // Kiểm tra và yêu cầu quyền đọc bộ nhớ ngoài nếu cần
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }

        init(view);

        setOnclick();

        updateImageCount();

        return view;
    }

    // Hàm ánh xạ view
    private void init(View view){
        btnAddImage = view.findViewById(R.id.add_image_layout);
        tvNumber = view.findViewById(R.id.tv_number);
        rcv = view.findViewById(R.id.recyclerView);
        tvEmptyMessage = view.findViewById(R.id.empty_image_message);
        tvSelectImage = view.findViewById(R.id.tv_select_image_hint);

        rcv.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new ImageUploadAdapter(getContext(), listImage, position);
        rcv.setAdapter(adapter);
    }

    // Hàm bắt sự kiện onclick
    private void setOnclick(){
        adapter.setOnImageClickListener(new ImageUploadAdapter.OnImageClickListener() {
            @Override
            public void onDeleteClick(int position) {
                if (listImage.isEmpty()) return;

                // Xóa ảnh đại diện nếu vị trí trùng với ảnh được xóa
                if (position == ImageFragment.this.position) {
                    listImage.remove(position);

                    // Đặt nhãn đại diện cho ảnh đầu tiên trong danh sách nếu còn ảnh
                    if (!listImage.isEmpty()) {
                        ImageFragment.this.position = 0;
                        room.setAvatar(ImageFragment.this.position); // Cập nhật vào ViewModel
                    } else {
                        ImageFragment.this.position = -1;
                        room.setAvatar(-1); // Cập nhật vào ViewModel
                    }
                } else if (position < ImageFragment.this.position) {
                    listImage.remove(position);
                    ImageFragment.this.position -= 1; // Cập nhật vị trí ảnh đại diện khi xóa ảnh trước đó
                    room.setAvatar(ImageFragment.this.position); // Cập nhật vào ViewModel
                } else {
                    listImage.remove(position);
                }

                adapter.setImageList(listImage);
                adapter.setRepresentativeImagePosition(ImageFragment.this.position);
                adapter.notifyDataSetChanged();
                updateImageCount();
            }

            @Override
            public void onImageClick(int position) {
                ImageFragment.this.position = position;
                room.setAvatar(ImageFragment.this.position); // Cập nhật vào ViewModel
                adapter.setRepresentativeImagePosition(position);
            }
        });

        btnAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        viewModel = new ViewModelProvider(requireActivity()).get(CreateRoomViewModel.class);
        room = viewModel.getRoom();
        if (room.getImages() != null) {
            listImage = new ArrayList<>(room.getImages());
        } else {
            listImage = new ArrayList<>();
        }

        // Đặt nhãn ảnh đại diện khi Fragment được gán
        position = room.getAvatar();
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Permission denied to read external storage", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    if (listImage.size() >= 6) {
                        Toast.makeText(getContext(), "Bạn chỉ được thêm tối đa 6 ảnh", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    listImage.add(imageUri.toString());
                }
            } else if (data.getData() != null) {
                if (listImage.size() < 6) {
                    Uri imageUri = data.getData();
                    listImage.add(imageUri.toString());
                } else {
                    Toast.makeText(getContext(), "Bạn chỉ được thêm tối đa 6 ảnh", Toast.LENGTH_SHORT).show();
                }
            }

            // Đặt nhãn "Ảnh đại diện" cho ảnh đầu tiên nếu chưa có ảnh đại diện
            if (position == -1 && !listImage.isEmpty()) {
                position = 0;
                room.setAvatar(position); // Cập nhật vào ViewModel
                adapter.setRepresentativeImagePosition(position);
            }

            adapter.notifyDataSetChanged();
            updateImageCount();
        } else {
            Log.e("ImageFragment", "Failed to select image");
        }
    }

    // Cập nhật số ảnh đã tải lên
    private void updateImageCount() {
        tvNumber.setText(String.valueOf(listImage.size()) + "/6");
        if (listImage.isEmpty()) {
            tvEmptyMessage.setVisibility(View.VISIBLE);
            tvSelectImage.setVisibility(View.GONE);
        } else {
            tvEmptyMessage.setVisibility(View.GONE);
            tvSelectImage.setVisibility(View.VISIBLE);
        }
    }

    // Getter và setter cho danh sách ảnh
    public List<String> getImageList() {
        return listImage;
    }

    public void setImageList(List<String> imageList) {
        if (imageList != null) {
            listImage = imageList;
            if (adapter != null) {
                adapter.setImageList(listImage);
                adapter.notifyDataSetChanged();
                updateImageCount();
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("imageList", new ArrayList<>(listImage));
        outState.putInt("representativeImagePosition", position);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            listImage = savedInstanceState.getStringArrayList("imageList");
            position = savedInstanceState.getInt("representativeImagePosition", -1);
            if (listImage != null) {
                adapter.setImageList(listImage);
                adapter.setRepresentativeImagePosition(position);
                updateImageCount();
            }
        }
    }
}
