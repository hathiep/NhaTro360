package com.example.nhatro360.controller.mainActivity.fragmentAccount;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.nhatro360.R;
import com.example.nhatro360.controller.Login;
import com.example.nhatro360.models.Room;
import com.example.nhatro360.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import com.google.firebase.firestore.FirebaseFirestore;
import androidx.fragment.app.FragmentManager;


public class AccountFragment extends Fragment {
    private ImageView imvEdit, imvPostedRoom, imvSavedRoom;
    private TextView btnLogout;
    private AccountViewModel viewModel;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        init(view);
        getCurrentUser();

        return view;
    }

    private void init(View view){
        imvEdit = view.findViewById(R.id.imv_edit);
        imvPostedRoom = view.findViewById(R.id.imv_posted_room);
        imvSavedRoom = view.findViewById(R.id.imv_saved_room);
        btnLogout = view.findViewById(R.id.btn_logout);
        viewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        // Thực hiện transaction để chuyển đến Fragment đích khi cần thiết
        imvPostedRoom.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new FragmentPostedRoom())
                    .addToBackStack("FragmentPostedRoom") // Đặt tên cụ thể cho back stack
                    .commit();
        });

        imvSavedRoom.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new FragmentSavedRoom())
                    .addToBackStack("FragmentSavedRoom") // Đặt tên cụ thể cho back stack
                    .commit();
        });
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLogoutDialgo();
            }
        });

    }

    private void getCurrentUser(){

        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            db.collection("users").whereEqualTo("email", userEmail)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            DocumentSnapshot userDoc = task.getResult().getDocuments().get(0);
                            User user = userDoc.toObject(User.class);
                            viewModel.setUser(user);
                        }
                    });
        }

    }

    private void showLogoutDialgo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Bạn có muốn đăng xuất?");
        builder.setMessage("Xác nhận đăng xuất khỏi ứng dụng?");

        // Nếu người dùng chọn Yes
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Thực hiện hành động khi người dùng chọn Yes
                auth.signOut();
                dialog.dismiss();
                show_dialog("Đăng xuất thành công!", 1);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(getContext(), Login.class);
                        startActivity(intent);
                    }
                }, 2000);
            }
        });

        // Nếu người dùng chọn Cancel hoặc nhấn back
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Thực hiện đóng Dialog khi người dùng chọn Cancel
                dialog.dismiss();
            }
        });

        // Hiển thị Dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void show_dialog(String s, int time){
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Thông báo");
        progressDialog.setMessage(s);
        progressDialog.show();

        // Sử dụng Handler để gửi một tin nhắn hoạt động sau một khoảng thời gian
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Ẩn Dialog sau khi đã qua một khoảng thời gian nhất định
                progressDialog.dismiss();
            }
        }, time * 1000); // Số milliseconds bạn muốn Dialog biến mất sau đó
    }
}
