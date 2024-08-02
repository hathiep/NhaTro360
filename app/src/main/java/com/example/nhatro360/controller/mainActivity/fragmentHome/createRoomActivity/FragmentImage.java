package com.example.nhatro360.controller.mainActivity.fragmentHome.createRoomActivity;

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

public class FragmentImage extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int STORAGE_PERMISSION_CODE = 123;

    private RelativeLayout mButtonAddImage;
    private TextView mTvNumber;
    private RecyclerView mRecyclerView;
    private ImageUploadAdapter mAdapter;
    private TextView mEmptyImageMessage, tvSelectImageHint;
    private List<String> mImageList;
    private Room room;
    private CreatRoomViewModel viewModel;
    private int mRepresentativeImagePosition = -1; // Vị trí của ảnh đại diện


    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image, container, false);

        // Kiểm tra và yêu cầu quyền đọc bộ nhớ ngoài nếu cần
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }

        mButtonAddImage = view.findViewById(R.id.add_image_layout);
        mTvNumber = view.findViewById(R.id.tv_number);
        mRecyclerView = view.findViewById(R.id.recyclerView);
        mEmptyImageMessage = view.findViewById(R.id.empty_image_message);
        tvSelectImageHint = view.findViewById(R.id.tv_select_image_hint);

        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        mAdapter = new ImageUploadAdapter(getContext(), mImageList, mRepresentativeImagePosition);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnImageClickListener(new ImageUploadAdapter.OnImageClickListener() {
            @Override
            public void onDeleteClick(int position) {
                if (mImageList.isEmpty()) return;

                // Xóa ảnh đại diện nếu vị trí trùng với ảnh được xóa
                if (position == mRepresentativeImagePosition) {
                    mImageList.remove(position);

                    // Đặt nhãn đại diện cho ảnh đầu tiên trong danh sách nếu còn ảnh
                    if (!mImageList.isEmpty()) {
                        mRepresentativeImagePosition = 0;
                        room.setAvatar(mRepresentativeImagePosition); // Cập nhật vào ViewModel
                    } else {
                        mRepresentativeImagePosition = -1;
                        room.setAvatar(-1); // Cập nhật vào ViewModel
                    }
                } else if (position < mRepresentativeImagePosition) {
                    mImageList.remove(position);
                    mRepresentativeImagePosition -= 1; // Cập nhật vị trí ảnh đại diện khi xóa ảnh trước đó
                    room.setAvatar(mRepresentativeImagePosition); // Cập nhật vào ViewModel
                } else {
                    mImageList.remove(position);
                }

                mAdapter.setImageList(mImageList);
                mAdapter.setRepresentativeImagePosition(mRepresentativeImagePosition);
                mAdapter.notifyDataSetChanged();
                updateImageCount();
            }

            @Override
            public void onImageClick(int position) {
                mRepresentativeImagePosition = position;
                room.setAvatar(mRepresentativeImagePosition); // Cập nhật vào ViewModel
                mAdapter.setRepresentativeImagePosition(position);
            }
        });

        mButtonAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        updateImageCount();

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        viewModel = new ViewModelProvider(requireActivity()).get(CreatRoomViewModel.class);
        room = viewModel.getRoom();
        if (room.getImages() != null) {
            mImageList = new ArrayList<>(room.getImages());
        } else {
            mImageList = new ArrayList<>();
        }

        // Đặt nhãn ảnh đại diện khi Fragment được gán
        mRepresentativeImagePosition = room.getAvatar();
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
                    if (mImageList.size() >= 6) {
                        Toast.makeText(getContext(), "Bạn chỉ được thêm tối đa 6 ảnh", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    mImageList.add(imageUri.toString());
                }
            } else if (data.getData() != null) {
                if (mImageList.size() < 6) {
                    Uri imageUri = data.getData();
                    mImageList.add(imageUri.toString());
                } else {
                    Toast.makeText(getContext(), "Bạn chỉ được thêm tối đa 6 ảnh", Toast.LENGTH_SHORT).show();
                }
            }

            // Đặt nhãn "Ảnh đại diện" cho ảnh đầu tiên nếu chưa có ảnh đại diện
            if (mRepresentativeImagePosition == -1 && !mImageList.isEmpty()) {
                mRepresentativeImagePosition = 0;
                room.setAvatar(mRepresentativeImagePosition); // Cập nhật vào ViewModel
                mAdapter.setRepresentativeImagePosition(mRepresentativeImagePosition);
            }

            mAdapter.notifyDataSetChanged();
            updateImageCount();
        } else {
            Log.e("FragmentImage", "Failed to select image");
        }
    }

    private void updateImageCount() {
        mTvNumber.setText(String.valueOf(mImageList.size()) + "/6");
        if (mImageList.isEmpty()) {
            mEmptyImageMessage.setVisibility(View.VISIBLE);
            tvSelectImageHint.setVisibility(View.GONE);
        } else {
            mEmptyImageMessage.setVisibility(View.GONE);
            tvSelectImageHint.setVisibility(View.VISIBLE);
        }
    }

    // Getter và setter cho danh sách ảnh
    public List<String> getImageList() {
        return mImageList;
    }

    public void setImageList(List<String> imageList) {
        if (imageList != null) {
            mImageList = imageList;
            if (mAdapter != null) {
                mAdapter.setImageList(mImageList);
                mAdapter.notifyDataSetChanged();
                updateImageCount();
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("imageList", new ArrayList<>(mImageList));
        outState.putInt("representativeImagePosition", mRepresentativeImagePosition);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mImageList = savedInstanceState.getStringArrayList("imageList");
            mRepresentativeImagePosition = savedInstanceState.getInt("representativeImagePosition", -1);
            if (mImageList != null) {
                mAdapter.setImageList(mImageList);
                mAdapter.setRepresentativeImagePosition(mRepresentativeImagePosition);
                updateImageCount();
            }
        }
    }
}
