package com.example.nhatro360.controller.authenActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nhatro360.R;
import com.example.nhatro360.controller.mainActivity.MainActivity;
import com.example.nhatro360.model.Validate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    TextInputEditText edtEmail, edtPassword;
    Button btnLogin;
    TextView tvForgotPassword, tvRegister, tvPolicy;
//    ProgressBar progressBar;
    FirebaseAuth mAuth;
    ImageView imvEye;
    Integer eye;

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null && currentUser.isEmailVerified()){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            final WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.setSystemBarsAppearance(
                        0,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                );
            }
            getWindow().setStatusBarColor(getColor(R.color.blue2));
            getWindow().setNavigationBarColor(getColor(R.color.blue2));
        } else {
            // For API < 30
            int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            getWindow().getDecorView().setSystemUiVisibility(flags);
            getWindow().setStatusBarColor(getResources().getColor(R.color.blue2));
            getWindow().setNavigationBarColor(getResources().getColor(R.color.blue2));
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Ánh xạ view
        initUi();
        // Set logic ẩn mật khẩu
        setUiEye();
        // Gọi các onClickListener
        onClickListener();
    }
    // Hàm ánh xạ view
    private void initUi(){
        mAuth = FirebaseAuth.getInstance();
        edtEmail = findViewById(R.id.edt_email);
        edtPassword = findViewById(R.id.edt_password);
        imvEye = findViewById(R.id.imV_eye);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        tvRegister = findViewById(R.id.tv_register);
        tvPolicy = findViewById(R.id.tv_policy);
        btnLogin = findViewById(R.id.btn_login);
    }
    // Hàm logic ẩn mật khẩu
    private void setUiEye(){
        eye = 0;
        imvEye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(eye == 0){
                    // Chuyển icon unhide thành hide
                    imvEye.setImageResource(R.drawable.ic_hide);
                    // Chuyển txt từ hide thành unhide
                    edtPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                    // Đặt con trỏ nháy ở cuối input đã nhập
                    edtPassword.setSelection(edtPassword.getText().length());
                    eye = 1;
                }
                else {
                    // Chuyển icon hide thành unhide
                    imvEye.setImageResource(R.drawable.ic_unhide);
                    // Chuyển text từ unhide thành hide
                    int inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
                    edtPassword.setInputType(inputType);
                    // Đặt con trỏ nháy ở cuối input đã nhập
                    edtPassword.setSelection(edtPassword.getText().length());
                    eye = 0;
                }
            }
        });
    }
    // Hàm gọi các onClick
    private void onClickListener(){
        // OnClick đổi mật khẩu
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ForgotPassword.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
        // OnClick đăng ký
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
        // OnClick Điều khoản và chính sách
        tvPolicy.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://thuenhatro360.com/dieu-khoan-su-dung"));
            startActivity(intent);
        });

        // OnClick đăng nhập
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lấy các giá trị từ input
                String email, password;
                email = edtEmail.getText().toString();
                password = edtPassword.getText().toString();
                // Gọi đối tượng validate
                Validate validate = new Validate(LoginActivity.this);
                if(!validate.validateLogin(email, password)) return;
                // Check đăng nhập
//                progressBar.setVisibility(View.VISIBLE);
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
//                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    checkVerified();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    show_dialog("Thông tin không đúng, vui lòng thử lại!", 2);

                                }
                            }
                        });
            }
        });
    }
    // Hàm kiểm tra đã xác thực email chưa
    private void checkVerified(){
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            if (user.isEmailVerified()) {
                // Email đã được xác thực, chuyển hướng người dùng đến màn hình chính
                show_dialog("Đăng nhập thành công!", 1);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        finish();
                    }
                }, 1000);

            } else {
                // Email chưa được xác thực, hiển thị thông báo hoặc hướng dẫn người dùng xác thực email
                show_dialog("Vui lòng xác thực email của bạn trước khi đăng nhập!", 3);
            }
        }
    }
    // Hàm hiển thị thông báo
    private void show_dialog(String s, int time){
        ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
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
        }, time * 1000); // Số milliseconds Dialog biến mất sau đó
    }
}