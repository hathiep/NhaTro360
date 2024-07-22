package com.example.nhatro360;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;

public class FragmentImage extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int STORAGE_PERMISSION_CODE = 123;

    private ImageView mAddImage;
    private TextView mTvNumber;
    private RecyclerView mRecyclerView;
    private ImageUploadAdapter mAdapter;
    private List<String> mImageList;
    private int mRepresentativeImagePosition = -1; // Vị trí của ảnh đại diện

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image, container, false);

        // Kiểm tra và yêu cầu quyền đọc bộ nhớ ngoài nếu cần
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }

        mAddImage = view.findViewById(R.id.icon_add_image);
        mTvNumber = view.findViewById(R.id.tv_number);
        mRecyclerView = view.findViewById(R.id.recyclerView);

        if (savedInstanceState == null) {
            mImageList = new ArrayList<>();
        } else {
            mImageList = savedInstanceState.getStringArrayList("imageList");
            mRepresentativeImagePosition = savedInstanceState.getInt("representativeImagePosition", -1);
        }

        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        mAdapter = new ImageUploadAdapter(getContext(), mImageList, mRepresentativeImagePosition);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnImageClickListener(new ImageUploadAdapter.OnImageClickListener() {
            @Override
            public void onDeleteClick(int position) {
                if (position == mRepresentativeImagePosition) {
                    mRepresentativeImagePosition = -1; // Nếu ảnh bị xóa là ảnh đại diện
                } else if (position < mRepresentativeImagePosition) {
                    mRepresentativeImagePosition--; // Cập nhật vị trí ảnh đại diện khi xóa ảnh trước đó
                }
                mImageList.remove(position);
                mAdapter.notifyItemRemoved(position);
                updateImageCount();
            }

            @Override
            public void onImageClick(int position) {
                mRepresentativeImagePosition = position;
                mAdapter.setRepresentativeImagePosition(position);
            }
        });

        mAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        updateImageCount();

        return view;
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
            mAdapter.notifyDataSetChanged();
            updateImageCount();
        } else {
            Log.e("FragmentImage", "Failed to select image");
        }
    }

    private void updateImageCount() {
        mTvNumber.setText(String.valueOf(mImageList.size()));
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
