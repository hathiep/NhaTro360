package com.example.nhatro360.controller.mainActivity.fragmentSearch;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.nhatro360.R;
import com.example.nhatro360.models.Room;
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
    private FragmentSingleListRoom fragmentSingleListRoom;
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
    private Button btnSearch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        init(view);

        return view;
    }

    private void init(View view){
        edtSearch = view.findViewById(R.id.edt_search);
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
                // Kiểm tra nếu EditText trống và thực hiện hàm x()
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
        View view = getView();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        fragmentSingleListRoom = new FragmentSingleListRoom();

        searchRooms(query, new FirestoreCallback() {
            @Override
            public void onCallback(List<Room> searchResults) {
                // Gửi kết quả tìm kiếm tới fragmentSingleListRoom
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("searchResults", (ArrayList<? extends Parcelable>) searchResults);
                fragmentSingleListRoom.setArguments(bundle);

                // Hiển thị fragmentSingleListRoom trong SearchFragment
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.replace(R.id.container_list_room, fragmentSingleListRoom);
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
                            rooms.add(room);
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
        return filteredRooms;
    }

    private interface FirestoreCallback {
        void onCallback(List<Room> searchResults);
    }

    private void showMenuDialog(View view) throws JSONException {
        overlay.setVisibility(View.VISIBLE);

        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_address);
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
                imvFilter.setVisibility(View.VISIBLE);
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
