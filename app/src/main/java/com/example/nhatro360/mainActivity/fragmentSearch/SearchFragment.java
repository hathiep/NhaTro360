package com.example.nhatro360.mainActivity.fragmentSearch;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.nhatro360.R;
import com.example.nhatro360.model.Room;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class SearchFragment extends Fragment  {
    private EditText edtSearch, edtProvince, edtDistrict;
    private ImageView imvDrop, imvFilter;
    private SearchedRoomsFragment fragmentSearchedRoom;
    private FrameLayout layoutListRoom;
    private FirebaseFirestore db;
    private View overlay;
    private Dialog dialog;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private TextView tvSearchAround;
    private LinearLayout layoutHistory;
    private ListView historyListView;
    private List<String> provinceIds, districtIds;
    private JSONArray provincesArray, districtsArray;
    private String provinceId, districtId, province, district;
    private List<Room> listSearchedRoom;
    private List<TextView> listTvPostType;
    private List<TextView> listTvRoomType;
    private List<TextView> listTvOrderType;
    private int[] listOption = new int[3];
    private Button btnSearch;
    private TextView btnClose, btnFilter;
    private int minPrice, maxPrice, minArea, maxArea;
    private int minValuePrice = 1, maxValuePrice = 20;
    private int minValueArea = 10, maxValueArea = 100;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        init(view);

        return view;
    }

    private void init(View view){
        edtSearch = view.findViewById(R.id.edt_search);
        progressBar = view.findViewById(R.id.progressBar);
        tvSearchAround = view.findViewById(R.id.tv_search_around);
        layoutHistory = view.findViewById(R.id.layout_history);
        historyListView = view.findViewById(R.id.lv_history);
        layoutListRoom = view.findViewById(R.id.container_list_room);
        imvDrop = view.findViewById(R.id.imv_drop);
        overlay = view.findViewById(R.id.overlay);
        province = district = provinceId = districtId = "";
        imvFilter = view.findViewById(R.id.imv_filter);
        imvFilter.setVisibility(View.GONE);

        db = FirebaseFirestore.getInstance();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        tvSearchAround.setOnClickListener(v -> {
            Log.d(TAG, "Current location TextView clicked");
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                getCurrentLocation();
            }
        });

        loadSearchHistory(view);

        edtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String searchText = edtSearch.getText().toString().trim();
                    if (!searchText.isEmpty()) {
                        performSearch(searchText);
                        imvFilter.setVisibility(View.VISIBLE);
                    }
                    return true;
                }
                return false;
            }
        });

        // Lắng nghe sự kiện thay đổi văn bản trong EditText
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Không cần sử dụng trong trường hợp này
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Không cần sử dụng trong trường hợp này
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    loadSearchHistory(view);
                }
            }
        });

        imvDrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    showMenuDialog(view);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                setupPopupMenus();
            }
        });

        imvFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    showFilterDialog(view);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });

    }

    private void showFilterDialog(View view) throws JSONException {
        overlay.setVisibility(View.VISIBLE);
        initFilter();
        initDrag(1, true);
        initDrag(1, false);
        initDrag(2, true);
        initDrag(2, false);

        dialog.show();

        dialog.setOnDismissListener(dialogInterface -> {
            // Hide overlay when dialog is dismissed
            overlay.setVisibility(View.GONE);
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterRoomsByOption();
            }
        });
    }

    private void initFilter(){
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_filter_room);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // Set dialog width to match parent and height to wrap content
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.BOTTOM;
        dialog.getWindow().setAttributes(layoutParams);

        listOption[0] = listOption[1] = listOption[2] = 0;
        minPrice = minValuePrice;
        maxPrice = maxValuePrice;
        minArea = minValueArea;
        maxArea = maxValueArea;

        listTvPostType = new ArrayList<>();
        listTvPostType.add(dialog.findViewById(R.id.tv_all_post_type));
        listTvPostType.add(dialog.findViewById(R.id.tv_rent_room));
        listTvPostType.add(dialog.findViewById(R.id.tv_share_room));

        listTvRoomType = new ArrayList<>();
        listTvRoomType.add(dialog.findViewById(R.id.tv_all_room_type));
        listTvRoomType.add(dialog.findViewById(R.id.tv_room));
        listTvRoomType.add(dialog.findViewById(R.id.tv_apartment));
        listTvRoomType.add(dialog.findViewById(R.id.tv_mini_apartment));
        listTvRoomType.add(dialog.findViewById(R.id.tv_house));

        listTvOrderType = new ArrayList<>();
        listTvOrderType.add(dialog.findViewById(R.id.tv_all_sort_type));
        listTvOrderType.add(dialog.findViewById(R.id.tv_latest));
        listTvOrderType.add(dialog.findViewById(R.id.tv_upPrice));
        listTvOrderType.add(dialog.findViewById(R.id.tv_downPrice));

        onClickList(listTvPostType, 0);
        onClickList(listTvRoomType, 1);
        onClickList(listTvOrderType, 2);

        btnClose = dialog.findViewById(R.id.btn_close);
        btnFilter = dialog.findViewById(R.id.btn_filter);
    }

    private void onClickList(List<TextView> listTv, int position){
        for (int i=0; i<listTv.size(); i++){
            TextView tv = listTv.get(i);
            int index = i;
            tv.setOnClickListener(view -> {
                for(int j=0; j<listTv.size(); j++){
                    listTv.get(j).setTextColor(getResources().getColor(R.color.blue2));
                    listTv.get(j).setBackgroundTintList(null);
                }
                listOption[position] = index;
                tv.setTextColor(getResources().getColor(R.color.white));
                tv.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.blue2)));
            });
        }
    }

    private void initDrag(int i, boolean isLeft) {
        TextView tvSelected;
        View handleLeft, handleRight, rangeView;
        RelativeLayout container;
        if (i == 1) {
            tvSelected = dialog.findViewById(R.id.tv_selected_price);
            handleLeft = dialog.findViewById(R.id.handleLeft_price);
            handleRight = dialog.findViewById(R.id.handleRight_price);
            rangeView = dialog.findViewById(R.id.rangeView_price);
            container = dialog.findViewById(R.id.container_price);
        } else {
            tvSelected = dialog.findViewById(R.id.tv_selected_area);
            handleLeft = dialog.findViewById(R.id.handleLeft_area);
            handleRight = dialog.findViewById(R.id.handleRight_area);
            rangeView = dialog.findViewById(R.id.rangeView_area);
            container = dialog.findViewById(R.id.container_area);
        }

        View handle = isLeft ? handleLeft : handleRight;
        handle.setOnTouchListener(new View.OnTouchListener() {
            private float dX;
            private boolean dragging = false;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = view.getX() - event.getRawX();
                        dragging = true;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (dragging) {
                            float newX = event.getRawX() + dX;
                            int handleWidth = view.getWidth();
                            int containerWidth = container.getWidth();

                            if (isLeft) {
                                newX = Math.max(0, newX);
                                newX = Math.min(handleRight.getX() - handleWidth, newX);
                            } else {
                                newX = Math.max(handleLeft.getX() + handleWidth, newX);
                                newX = Math.min(containerWidth - handleWidth, newX);
                            }

                            view.animate().x(newX).setDuration(0).start();

                            float leftPos = handleLeft.getX();
                            float rightPos = handleRight.getX();

                            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) rangeView.getLayoutParams();
                            layoutParams.width = (int) (rightPos - leftPos + handleWidth);
                            layoutParams.setMargins((int) leftPos, layoutParams.topMargin, 0, layoutParams.bottomMargin);
                            rangeView.setLayoutParams(layoutParams);

                            if (i == 1) {
                                minPrice = minValuePrice + (int) ((leftPos / containerWidth) * (maxValuePrice - minValuePrice + 2));
                                maxPrice = minValuePrice + (int) ((rightPos / containerWidth) * (maxValuePrice - minValuePrice + 2));
                                String selected = minPrice + " triệu  - " + maxPrice;
                                if(maxPrice == maxValuePrice) selected+= "+ triệu";
                                else selected+= " triệu";
                                tvSelected.setText(selected);
                            } else {
                                minArea = minValueArea + (int) ((leftPos / containerWidth) * (maxValueArea - minValueArea + 6));
                                maxArea = minValueArea + (int) ((rightPos / containerWidth) * (maxValueArea - minValueArea + 6));
                                String selected = minArea + " m2  - " + maxArea;
                                if(maxArea == maxValueArea) selected+= "+ m2";
                                else selected+= " m2";
                                tvSelected.setText(selected);
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        dragging = false;
                        break;
                }
                return true;
            }
        });
    }

    private void filterRoomsByOption() {
        progressBar.setVisibility(View.VISIBLE);
        int postType = listOption[0];
        int roomType = listOption[1];
        int orderType = listOption[2];

        Log.e(TAG, "PostType = " + postType);
        Log.e(TAG, "RoomType = " + roomType);
        Log.e(TAG, "OrderType = " + orderType);
        Log.e(TAG, "Price = " + minPrice + " - " + maxPrice);
        Log.e(TAG, "Area = " + minArea + " - " + maxArea);
        Log.e(TAG, "" + listSearchedRoom.size());

        // Lọc danh sách phòng dựa trên các điều kiện
        List<Room> filteredRooms = new ArrayList<>();
        for (Room room : listSearchedRoom) {
            boolean matches = true;

            if (postType != 0 && room.getPostType() != postType) {
                matches = false;
            }

            // Lọc theo roomType
            if (roomType != 0 && room.getRoomType() != roomType) {
                matches = false;
            }

            int price = Integer.parseInt(room.getPrice());
            if (price < minPrice * 1000000 || price > maxPrice * 1000000) {
                matches = false;
            }

            int area = Integer.parseInt(room.getArea());
            if (area < minArea || area > maxArea) {
                matches = false;
            }

            if (matches) {
                filteredRooms.add(room);
            }
        }

        // Sắp xếp danh sách phòng dựa trên điều kiện orderType
        switch (orderType) {
            case 1:
                Collections.sort(filteredRooms, new Comparator<Room>() {
                    @Override
                    public int compare(Room r1, Room r2) {
                        return r2.getTimePosted().compareTo(r1.getTimePosted()); // Sắp xếp theo thời gian mới nhất
                    }
                });
                break;
            case 2:
                Collections.sort(filteredRooms, new Comparator<Room>() {
                    @Override
                    public int compare(Room r1, Room r2) {
                        return Integer.compare(Integer.parseInt(r1.getPrice()), Integer.parseInt(r2.getPrice())); // Sắp xếp theo giá tăng dần
                    }
                });
                break;
            case 3:
                Collections.sort(filteredRooms, new Comparator<Room>() {
                    @Override
                    public int compare(Room r1, Room r2) {
                        return Integer.compare(Integer.parseInt(r2.getPrice()), Integer.parseInt(r1.getPrice())); // Sắp xếp theo giá giảm dần
                    }
                });
                break;
        }

        // Ghi log danh sách phòng đã lọc
        for (Room room : filteredRooms) {
            Log.e(TAG, "" + room.getPrice());
            Log.e(TAG, "" + room.getPostType());
        }
        dialog.dismiss();

        new Handler().postDelayed(() -> {
            fragmentSearchedRoom.updateRoomList(filteredRooms);
            progressBar.setVisibility(View.GONE);
        }, 1000);
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), location -> {
                    if (location != null) {
                        Log.d(TAG, "Location obtained: " + location.toString());
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        updateAddressFields(latitude, longitude);
                    } else {
                        Log.d(TAG, "Location is null");
                    }
                });
    }

    private void updateAddressFields(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String province = address.getAdminArea();
                String district = address.getSubAdminArea();
                String str = district + ", " + province;
                if(district.equals("")) str = province;
                performSearch(str);
            } else {
                Log.d(TAG, "No addresses found");
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoder exception", e);
        }
    }

    private void loadSearchHistory(View view) {
        layoutHistory.setVisibility(View.VISIBLE);
        layoutListRoom.setVisibility(View.GONE);
        imvFilter.setVisibility(View.GONE);
        if (getContext() != null) {
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("search_history", Context.MODE_PRIVATE);
            String history = sharedPreferences.getString("history", "");

            if (!history.isEmpty()) {
                String[] historyArray = history.split(";");

                // Chỉ lấy 5 mục gần nhất
                List<String> historyList = Arrays.asList(historyArray);
                if (historyList.size() > 5) {
                    historyList = historyList.subList(0, 5);
                }

                // Tạo một ArrayAdapter
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        getContext(),
                        R.layout.item_list_history_search, R.id.text,
                        historyList
                );

                historyListView.setAdapter(adapter);

                // Đặt OnItemClickListener cho ListView
                historyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String query = (String) parent.getItemAtPosition(position);
                        edtSearch.setText(query);
                        performSearch(query);
                    }
                });
            }
        }
    }

    private void performSearch(String query) {
        saveSearchHistory(query);
        edtSearch.setText(query);
        layoutListRoom.setVisibility(View.VISIBLE);
        listSearchedRoom = new ArrayList<>();
        View view = getView();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        fragmentSearchedRoom = new SearchedRoomsFragment();

        searchRooms(query, new FirestoreCallback() {
            @Override
            public void onCallback(List<Room> searchResults) {
                // Gửi kết quả tìm kiếm tới fragmentSearchedRoom
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("searchResults", (ArrayList<? extends Parcelable>) searchResults);
                fragmentSearchedRoom.setArguments(bundle);

                // Hiển thị fragmentSearchedRoom trong SearchFragment
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.replace(R.id.container_list_room, fragmentSearchedRoom);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
    }

    private void searchRooms(String query, FirestoreCallback callback) {
        db.collection("rooms")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Room> rooms = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String roomId = document.getId(); // Lấy ID của tài liệu Firestore
                            Room room = document.toObject(Room.class);
                            room.setId(roomId);
                            if (room.getStatus() == 1) {
                                rooms.add(room);
                            }
                        }
                        // Lọc kết quả tìm kiếm trên thiết bị
                        List<Room> filteredRooms = filterRoomsByQuery(rooms, query);
                        callback.onCallback(filteredRooms);
                    } else {
                        Log.e("Firestore", "Error getting documents: ", task.getException());
                    }
                });
    }

    private List<Room> filterRoomsByQuery(List<Room> rooms, String query) {
        List<Room> filteredRooms = new ArrayList<>();
        for (Room room : rooms) {
            if (room.getAddress() != null && room.getAddress().toLowerCase().contains(query.toLowerCase())) {
                filteredRooms.add(room);
            }
        }
        listSearchedRoom = filteredRooms;
        return filteredRooms;
    }

    private interface FirestoreCallback {
        void onCallback(List<Room> searchResults);
    }

    private void showMenuDialog(View view) throws JSONException {
        overlay.setVisibility(View.VISIBLE);

        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_search_address);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Set dialog width to match parent and height to wrap content
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.BOTTOM;
        dialog.getWindow().setAttributes(layoutParams);

        edtProvince = dialog.findViewById(R.id.edt_province);
        edtDistrict = dialog.findViewById(R.id.edt_district);
        try {
            getArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        provinceIds = new ArrayList<>();
        for (int i = 0; i < provincesArray.length(); i++) {
            JSONObject provinceObject = provincesArray.getJSONObject(i);
            provinceIds.add(provinceObject.getString("idProvince"));
        }
        if (province != null) {
            int provincePosition = findProvincePosition(province.trim());
            if (provincePosition >= 0) {
                edtProvince.setText(province);
                provinceId = provinceIds.get(provincePosition);
                fetchDistricts(provinceId);
            } else {
                Log.e(TAG, "Province not found or index out of bounds " + provincePosition);
            }
        }
        districtIds = new ArrayList<>();
        for (int i = 0; i < districtsArray.length(); i++) {
            JSONObject districtObject = districtsArray.getJSONObject(i);
            districtIds.add(districtObject.getString("idDistrict"));
        }
        if (district != null) {
            int districtPosition = findDistrictPosition(district.trim(), provinceId);
            if (districtPosition >= 0) {
                edtDistrict.setText(district);
                districtId = districtIds.get(districtPosition);
            } else {
                Log.e(TAG, "District not found or index out of bounds " + districtPosition);
            }
        }
        edtProvince.setText(province);
        edtDistrict.setText(district);
        btnSearch = dialog.findViewById(R.id.btn_search);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(edtProvince.getText().toString().equals("")){
                    Toast.makeText(getActivity(), "Vui lòng chọn Tỉnh/TP", Toast.LENGTH_SHORT).show();
                    return;
                }
                overlay.setVisibility(View.GONE);
                dialog.dismiss();
                province = edtProvince.getText().toString();
                district = edtDistrict.getText().toString();
                String str = district + ", " + province;
                if(district.equals("")) str = province;
                performSearch(str);
            }
        });
        // Show dialog
        dialog.show();

        dialog.setOnDismissListener(dialogInterface -> {
            // Hide overlay when dialog is dismissed
            overlay.setVisibility(View.GONE);
        });

    }

    private void saveSearchHistory(String query) {
        layoutHistory.setVisibility(View.GONE);
        imvFilter.setVisibility(View.VISIBLE);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("search_history", Context.MODE_PRIVATE);
        String history = sharedPreferences.getString("history", "");

        List<String> historyList;
        if (!history.isEmpty()) {
            historyList = new ArrayList<>(Arrays.asList(history.split(";")));
        } else {
            historyList = new ArrayList<>();
        }

        // Kiểm tra nếu truy vấn đã tồn tại trong danh sách lịch sử
        if (!historyList.contains(query)) {
            // Thêm mục mới vào đầu danh sách
            historyList.add(0, query);

            // Giới hạn số lượng mục trong danh sách
            if (historyList.size() > 5) {
                historyList = historyList.subList(0, 5);
            }

            // Lưu danh sách lịch sử vào SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("history", String.join(";", historyList));
            editor.apply();
        }
    }

    private void getArray() throws IOException, JSONException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(getActivity().getAssets().open("provinces.json")));
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonBuilder.append(line);
        }
        JSONObject jsonObject = new JSONObject(jsonBuilder.toString());
        provincesArray = sortArray(jsonObject.getJSONArray("province"), true);
        districtsArray = sortArray(jsonObject.getJSONArray("district"), false);
    }

    private JSONArray sortArray(JSONArray arr, boolean prov) throws JSONException {
        List<JSONObject> list = new ArrayList<>();
        int x = 0;
        if(prov) x = 5; // 5 Thành phố trực thuộc trung ương ưu tiên lên đầu không sắp xếp
        for (int i = x; i < arr.length(); i++) {
            list.add(arr.getJSONObject(i));
        }
        Collections.sort(list, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject a, JSONObject b) {
                String valA = a.optString("name");
                String valB = b.optString("name");
                return valA.compareTo(valB);
            }
        });

        JSONArray sortedJsonArray = new JSONArray();
        if(prov) for(int i=0; i<5; i++) sortedJsonArray.put(arr.get(i));
        for (JSONObject object : list) sortedJsonArray.put(object);
        return sortedJsonArray;
    }

    private int findProvincePosition(String province) {
        for (int i = 0; i < provincesArray.length(); i++) {
            try {
                if (provincesArray.getJSONObject(i).getString("name").equals(province)) {
                    return i;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    private int findDistrictPosition(String district, String provinceId) {
        for (int i = 0; i < districtsArray.length(); i++) {
            try {
                if (districtsArray.getJSONObject(i).getString("idProvince").equals(provinceId) && districtsArray.getJSONObject(i).getString("name").equals(district)) {
                    return i;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    private void fetchDistricts(String provinceId) throws JSONException {
        List<String> districtList = new ArrayList<>();
        districtIds = new ArrayList<>();

        if (provinceId.equals("")) {
            Toast.makeText(getActivity(), "Vui lòng chọn Tỉnh/TP", Toast.LENGTH_SHORT).show();
            return;
        }
        for (int i = 0; i < districtsArray.length(); i++) {
            JSONObject districtObject = districtsArray.getJSONObject(i);
            if (districtObject.getString("idProvince").equals(provinceId)) {
                districtList.add(districtObject.getString("name"));
                districtIds.add(districtObject.getString("idDistrict"));
            }
        }

        edtDistrict.setOnClickListener(v -> showListViewPopup(v, districtList, (position) -> {
            districtIds.clear();
            for (int i = 0; i < districtsArray.length(); i++) {
                JSONObject districtObject = districtsArray.getJSONObject(i);
                if (districtObject.getString("idProvince").equals(provinceId)) {
                    districtList.add(districtObject.getString("name"));
                    districtIds.add(districtObject.getString("idDistrict"));
                }
            }
            edtDistrict.setText(districtList.get(position));
            districtId = districtIds.get(position);
        }));

    }

    private void showListViewPopup(View anchor, List<String> items, OnItemClickListener listener) {
        if (items.size() == 0) {
            Toast.makeText(getActivity(), "Danh sách trống", Toast.LENGTH_SHORT).show();
            return;
        }

        ListView listView = dialog.findViewById(R.id.list_view_popup);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getActivity(),
                R.layout.item_list_seach_address,  // Sử dụng layout của bạn
                items
        );
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);

        listView.setVisibility(View.VISIBLE);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            try {
                listener.onItemClick(position);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });

        dialog.show();
    }

    private void setupPopupMenus() {
        List<String> provinces = new ArrayList<>();
        provinceIds = new ArrayList<>();

        try {
            for (int i = 0; i < provincesArray.length(); i++) {
                JSONObject provinceObject = provincesArray.getJSONObject(i);
                provinces.add(provinceObject.getString("name"));
                provinceIds.add(provinceObject.getString("idProvince"));
            }

            edtProvince.setOnClickListener(v -> showListViewPopup(v, provinces, (position) -> {
                edtProvince.setText(provinces.get(position));
                provinceId = provinceIds.get(position);
                edtDistrict.setText("");
                districtIds = new ArrayList<>();
                districtId = "";
                fetchDistricts(provinceId);
            }));

            edtDistrict.setOnClickListener(v -> {
                if (provinceId.equals("")) {
                    Toast.makeText(getActivity(), "Vui lòng chọn Tỉnh/TP", Toast.LENGTH_SHORT).show();
                    return;
                }
                List<String> districtList = new ArrayList<>();
                districtIds = new ArrayList<>(); // Ensure this is cleared and repopulated correctly
                try {
                    for (int i = 0; i < districtsArray.length(); i++) {
                        JSONObject districtObject = districtsArray.getJSONObject(i);
                        if (districtObject.getString("idProvince").equals(provinceId)) {
                            districtList.add(districtObject.getString("name"));
                            districtIds.add(districtObject.getString("idDistrict")); // Add district IDs corresponding to province
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                showListViewPopup(v, districtList, (position) -> {
                    edtDistrict.setText(districtList.get(position));
                    districtId = districtIds.get(position);
                });
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    interface OnItemClickListener {
        void onItemClick(int position) throws JSONException;
    }

}
