package com.example.nhatro360;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

public class ImageUploadAdapter extends RecyclerView.Adapter<ImageUploadAdapter.ImageViewHolder> {
    private Context mContext;
    private List<String> mImageList;
    private OnImageClickListener mListener;
    private int mRepresentativeImagePosition = -1; // Vị trí của ảnh đại diện

    public void setImageList(List<String> mImageList) {
        this.mImageList = mImageList;
    }

    public void setRepresentativeImagePosition(int position) {
        mRepresentativeImagePosition = position;
        notifyDataSetChanged(); // Cập nhật lại giao diện khi ảnh đại diện thay đổi
    }

    public interface OnImageClickListener {
        void onDeleteClick(int position);
        void onImageClick(int position); // Thêm phương thức để xử lý khi click vào ảnh
    }

    public void setOnImageClickListener(OnImageClickListener listener) {
        mListener = listener;
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public ImageButton buttonDelete;
        public TextView representativeLabel; // Thêm TextView để hiển thị nhãn ảnh đại diện

        public ImageViewHolder(View itemView, final OnImageClickListener listener) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            buttonDelete = itemView.findViewById(R.id.button_delete);
            representativeLabel = itemView.findViewById(R.id.representative_label); // Ánh xạ TextView

            buttonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onDeleteClick(position);
                        }
                    }
                }
            });

            imageView.setOnClickListener(new View.OnClickListener() { // Thêm sự kiện click cho imageView
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onImageClick(position);
                        }
                    }
                }
            });
        }
    }

    public ImageUploadAdapter(Context context, List<String> imageList, int representativeImagePosition) {
        mContext = context;
        mImageList = imageList;
        mRepresentativeImagePosition = representativeImagePosition;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_image2, parent, false);
        return new ImageViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUriString = mImageList.get(position);
        Uri imageUri = Uri.parse(imageUriString);
        Glide.with(mContext)
                .load(imageUri)
                .into(holder.imageView);

        if (position == mRepresentativeImagePosition) {
            holder.representativeLabel.setVisibility(View.VISIBLE);
        } else {
            holder.representativeLabel.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mImageList.size();
    }
}
