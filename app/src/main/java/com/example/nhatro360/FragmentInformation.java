package com.example.nhatro360;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FragmentInformation extends Fragment {

    private TextView tvRentRoom, tvShareRoom;
    private List<TextView> listTvRoomType;
    private EditText edtRoomPrice, edtRoomArea, edtElectricPrice, edtWaterPrice;
    private List<ImageView> listImvUtilites = new ArrayList<>();
    private List<TextView> listTvUtilites = new ArrayList<>();
    private static List<Boolean> utilities;
    private Integer postType, roomType;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_information, container, false);

        init(view);

        setOnclickTextView();

        return view;
    }

    private void init(View view){
        postType = 0;
        roomType = 0;
        tvRentRoom = view.findViewById(R.id.tv_rent_room);
        tvShareRoom = view.findViewById(R.id.tv_share_room);
        listTvRoomType = new ArrayList<>();
        listTvRoomType.add(view.findViewById(R.id.tv_room));
        listTvRoomType.add(view.findViewById(R.id.tv_apartment));
        listTvRoomType.add(view.findViewById(R.id.tv_mini_apartment));
        listTvRoomType.add(view.findViewById(R.id.tv_house));
        edtRoomPrice = view.findViewById(R.id.edt_room_price);
        edtRoomArea = view.findViewById(R.id.edt_room_area);
        listImvUtilites.add(view.findViewById(R.id.imv_wifi));
        listImvUtilites.add(view.findViewById(R.id.imv_wc));
        listImvUtilites.add(view.findViewById(R.id.imv_parking));
        listImvUtilites.add(view.findViewById(R.id.imv_free_time));
        listImvUtilites.add(view.findViewById(R.id.imv_kitchen));
        listImvUtilites.add(view.findViewById(R.id.imv_air_conditioner));
        listImvUtilites.add(view.findViewById(R.id.imv_fridge));
        listImvUtilites.add(view.findViewById(R.id.imv_washing_machine));
        listTvUtilites.add(view.findViewById(R.id.tv_wifi));
        listTvUtilites.add(view.findViewById(R.id.tv_wc));
        listTvUtilites.add(view.findViewById(R.id.tv_parking));
        listTvUtilites.add(view.findViewById(R.id.tv_free_time));
        listTvUtilites.add(view.findViewById(R.id.tv_kitchen));
        listTvUtilites.add(view.findViewById(R.id.tv_air_conditioner));
        listTvUtilites.add(view.findViewById(R.id.tv_fridge));
        listTvUtilites.add(view.findViewById(R.id.tv_washing_machine));
        utilities = new ArrayList<>(Collections.nCopies(7, Boolean.FALSE));
    }

    private void setOnclickTextView(){
        onClickPostType(tvRentRoom, tvShareRoom, 0);
        onClickPostType(tvShareRoom, tvRentRoom, 1);
        for(int i=0; i<4; i++) onClickRoomType(listTvRoomType.get(i), i);
        onClickUtilities();
    }

    private void onClickPostType(TextView tv1, TextView tv2, int type){
        tv1.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {
                postType = type;
                tv1.setTextColor(getResources().getColor(R.color.white));
                tv1.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.blue2)));
                tv2.setTextColor(getResources().getColor(R.color.blue2));
                tv2.setBackgroundTintList(null);
            }
        });
    }

    private void onClickRoomType(TextView tv, int type){
        tv.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {
                for(int i=0; i<4; i++){
                    listTvRoomType.get(i).setTextColor(getResources().getColor(R.color.blue2));
                    listTvRoomType.get(i).setBackgroundTintList(null);
                }
                roomType = type;
                tv.setTextColor(getResources().getColor(R.color.white));
                tv.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.blue2)));
            }
        });
    }

    private void onClickUtilities(){
        for(int i=0; i<7; i++){
            ImageView imv = listImvUtilites.get(i);
            TextView tv = listTvUtilites.get(i);
            int index = i;
            imv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!utilities.get(index)){
                        imv.setColorFilter(ContextCompat.getColor(imv.getContext(), R.color.blue2), PorterDuff.Mode.SRC_IN);
                        tv.setTextColor(ContextCompat.getColor(tv.getContext(), R.color.blue2));
                        Toast.makeText(getContext(), getRoomPrice(), Toast.LENGTH_SHORT).show();
                        utilities.set(index, true);
                    }
                    else {
                        imv.setColorFilter(ContextCompat.getColor(imv.getContext(), R.color.gray3), PorterDuff.Mode.SRC_IN);
                        tv.setTextColor(ContextCompat.getColor(tv.getContext(), R.color.gray3));
                        utilities.set(index, false);
                    }
                }
            });
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!utilities.get(index)){
                        imv.setColorFilter(ContextCompat.getColor(imv.getContext(), R.color.blue2), PorterDuff.Mode.SRC_IN);
                        tv.setTextColor(ContextCompat.getColor(tv.getContext(), R.color.blue2));
                        utilities.set(index, true);
                    }
                    else {
                        imv.setColorFilter(ContextCompat.getColor(imv.getContext(), R.color.gray3), PorterDuff.Mode.SRC_IN);
                        tv.setTextColor(ContextCompat.getColor(tv.getContext(), R.color.gray3));
                        utilities.set(index, false);
                    }
                }
            });
        }
    }

    public int getPostType(){
        return postType;
    }

    public int getRoomType(){
        return roomType;
    }

    public String getRoomPrice(){
        return edtRoomPrice.getText().toString();
    }

    public String getRoomArea(){
        return edtRoomArea.getText().toString();
    }

    public List<Boolean> getUtilities(){
        return utilities;
    }
}
