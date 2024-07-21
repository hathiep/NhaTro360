package com.example.nhatro360;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class FragmentInformation extends Fragment {

    private TextView tvRentRoom, tvShareRoom, tvRoom, tvApartment, tvMiniApartment, tvHouse;
    private List<TextView> listTvRoomType;
    private EditText edtRoomPrice, edtArea, edtElectricPrice, edtWaterPrice;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_information, container, false);

        init(view);

        setOnclickTextView();

        return view;
    }

    private void init(View view){
        tvRentRoom = view.findViewById(R.id.tv_rent_room);
        tvShareRoom = view.findViewById(R.id.tv_share_room);
//        tvRoom = view.findViewById(R.id.tv_room);
//        tvApartment = view.findViewById(R.id.tv_apartment);
//        tvMiniApartment = view.findViewById(R.id.tv_mini_apartment);
//        tvHouse = view.findViewById(R.id.tv_house);
        listTvRoomType = new ArrayList<>();
        listTvRoomType.add(view.findViewById(R.id.tv_room));
        listTvRoomType.add(view.findViewById(R.id.tv_apartment));
        listTvRoomType.add(view.findViewById(R.id.tv_mini_apartment));
        listTvRoomType.add(view.findViewById(R.id.tv_house));
    }

    private void setOnclickTextView(){
        onClickPostType(tvRentRoom, tvShareRoom);
        onClickPostType(tvShareRoom, tvRentRoom);
        for(int i=0; i<4; i++) onClickRoomType(listTvRoomType.get(i));
    }

    private void onClickPostType(TextView tv1, TextView tv2){
        tv1.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {
                tv1.setTextColor(getResources().getColor(R.color.white));
                tv1.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.blue2)));
                tv2.setTextColor(getResources().getColor(R.color.blue2));
                tv2.setBackgroundTintList(null);
            }
        });
    }

    private void onClickRoomType(TextView tv){
        tv.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {
                for(int i=0; i<4; i++){
                    listTvRoomType.get(i).setTextColor(getResources().getColor(R.color.blue2));
                    listTvRoomType.get(i).setBackgroundTintList(null);
                }
                tv.setTextColor(getResources().getColor(R.color.white));
                tv.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.blue2)));
            }
        });
    }
}
