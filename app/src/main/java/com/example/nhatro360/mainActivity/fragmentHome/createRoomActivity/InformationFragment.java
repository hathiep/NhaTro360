package com.example.nhatro360.mainActivity.fragmentHome.createRoomActivity;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.nhatro360.R;
import com.example.nhatro360.model.Room;

import java.util.ArrayList;
import java.util.List;

public class InformationFragment extends Fragment {

    private TextView tvRentRoom, tvShareRoom;
    private List<TextView> listTvRoomType;
    private EditText edtRoomPrice, edtRoomArea, edtElectricPrice, edtWaterPrice;
    private List<ImageView> listImvUtilites;
    private List<TextView> listTvUtilites;
    private List<Boolean> utilities;
    private CreateRoomViewModel viewModel;
    private Room room;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_information, container, false);

        init(view);

        setOnclickTextView();

        return view;
    }

    // Hàm ánh xạ view
    private void init(View view){
        viewModel = new ViewModelProvider(requireActivity()).get(CreateRoomViewModel.class);
        room = viewModel.getRoom();
        utilities = room.getUtilities();
        tvRentRoom = view.findViewById(R.id.tv_rent_room);
        tvShareRoom = view.findViewById(R.id.tv_share_room);
        listTvRoomType = new ArrayList<>();
        listTvRoomType.add(view.findViewById(R.id.tv_room));
        listTvRoomType.add(view.findViewById(R.id.tv_apartment));
        listTvRoomType.add(view.findViewById(R.id.tv_mini_apartment));
        listTvRoomType.add(view.findViewById(R.id.tv_house));
        edtRoomPrice = view.findViewById(R.id.edt_room_price);
        edtRoomArea = view.findViewById(R.id.edt_room_area);
        listImvUtilites = new ArrayList<>();
        listImvUtilites.add(view.findViewById(R.id.imv_wifi));
        listImvUtilites.add(view.findViewById(R.id.imv_wc));
        listImvUtilites.add(view.findViewById(R.id.imv_parking));
        listImvUtilites.add(view.findViewById(R.id.imv_free_time));
        listImvUtilites.add(view.findViewById(R.id.imv_kitchen));
        listImvUtilites.add(view.findViewById(R.id.imv_air_conditioner));
        listImvUtilites.add(view.findViewById(R.id.imv_fridge));
        listImvUtilites.add(view.findViewById(R.id.imv_washing_machine));
        listTvUtilites = new ArrayList<>();
        listTvUtilites.add(view.findViewById(R.id.tv_wifi));
        listTvUtilites.add(view.findViewById(R.id.tv_wc));
        listTvUtilites.add(view.findViewById(R.id.tv_parking));
        listTvUtilites.add(view.findViewById(R.id.tv_free_time));
        listTvUtilites.add(view.findViewById(R.id.tv_kitchen));
        listTvUtilites.add(view.findViewById(R.id.tv_air_conditioner));
        listTvUtilites.add(view.findViewById(R.id.tv_fridge));
        listTvUtilites.add(view.findViewById(R.id.tv_washing_machine));

        // Set dữ liệu từ ViewModel
        edtRoomPrice.setText(room.getPrice());
        edtRoomArea.setText(room.getArea());

        // Update thuộc tính từ dữ liệu trên View Model (nếu có)
        updateUIBasedOnRoomData();
    }

    private void updateUIBasedOnRoomData() {
        // Update post type
        updatePostType((room.getPostType() == 0) ? tvRentRoom : tvShareRoom,
                (room.getPostType() == 0) ? tvShareRoom : tvRentRoom);
        updateRoomType(listTvRoomType.get(room.getRoomType()));
        for(int i=0; i<8; i++){
            updateUtilityUI(listImvUtilites.get(i), listTvUtilites.get(i), utilities.get(i));
        }
    }

    // Hàm bắt sự kiện click các lựa chọn
    private void setOnclickTextView(){
        // Nếu Cho thuê được click
        onClickPostType(tvRentRoom, tvShareRoom, 0);
        // Nếu Tìm người ở ghép được click
        onClickPostType(tvShareRoom, tvRentRoom, 1);
        // Bắt sự kiện click 4 loại phòng
        for(int i=0; i<4; i++) onClickRoomType(listTvRoomType.get(i), i);
        // Bắt sự kiện click tiện ích phòng
        onClickUtilities();
    }

    private void onClickPostType(TextView tv1, TextView tv2, int type){
        tv1.setOnClickListener(view -> {
            room.setPostType(type);
            updatePostType(tv1, tv2);
            viewModel.setRoom(room);
        });
    }

    private void updatePostType(TextView tv1, TextView tv2){
        // TV được chọn màu nền xanh, màu chữ trắng
        tv1.setTextColor(getResources().getColor(R.color.white));
        tv1.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.blue2)));
        // Tv còn lại màu nền trắng, màu chữ xanh
        tv2.setTextColor(getResources().getColor(R.color.blue2));
        tv2.setBackgroundTintList(null);
    }

    private void onClickRoomType(TextView tv, int type){
        tv.setOnClickListener(view -> {
            updateRoomType(tv);
            room.setRoomType(type);
            viewModel.setRoom(room);
        });
    }

    private void updateRoomType(TextView tv){
        // Chuyển hết tv thành màu nền trắng, màu chữ xanh
        for(int i=0; i<4; i++){
            listTvRoomType.get(i).setTextColor(getResources().getColor(R.color.blue2));
            listTvRoomType.get(i).setBackgroundTintList(null);
        }
        // TV được chọn màu nền xanh, màu chữ trắng
        tv.setTextColor(getResources().getColor(R.color.white));
        tv.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.blue2)));
    }

    private void onClickUtilities(){
        for(int i=0; i<8; i++){
            ImageView imv = listImvUtilites.get(i);
            TextView tv = listTvUtilites.get(i);
            int index = i;
            imv.setOnClickListener(view -> {
                utilities.set(index, !utilities.get(index));
                updateUtilityUI(imv, tv, utilities.get(index));
                room.setUtilities(utilities);
                viewModel.setRoom(room);
            });
            tv.setOnClickListener(view -> {
                utilities.set(index, !utilities.get(index));
                updateUtilityUI(imv, tv, utilities.get(index));
                room.setUtilities(utilities);
                viewModel.setRoom(room);
            });
        }
    }

    private void updateUtilityUI(ImageView imv, TextView tv, boolean isEnabled) {
        if (isEnabled) {
            // imv và tv được chọn màu xanh
            imv.setColorFilter(ContextCompat.getColor(imv.getContext(), R.color.blue2), PorterDuff.Mode.SRC_IN);
            tv.setTextColor(ContextCompat.getColor(tv.getContext(), R.color.blue2));
        } else {
            // imv và tv bỏ chọn màu xám
            imv.setColorFilter(ContextCompat.getColor(imv.getContext(), R.color.gray3), PorterDuff.Mode.SRC_IN);
            tv.setTextColor(ContextCompat.getColor(tv.getContext(), R.color.gray3));
        }
    }

    public Room getRoom(){
        room.setPrice(edtRoomPrice.getText().toString());
        room.setArea(edtRoomArea.getText().toString());
        return room;
    }
}
