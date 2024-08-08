package com.example.nhatro360.mainActivity.fragmentAccount;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nhatro360.R;
import com.example.nhatro360.authenActivity.LoginActivity;
import com.example.nhatro360.model.Validate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePassword extends AppCompatActivity {
    TextInputEditText edt_old_password, edt_new_password, edt_new_password_again;
    Button btn_change_password;
    ImageView imV_back, imV_eye1, imV_eye2, imV_eye3;
    FirebaseAuth auth;
    FirebaseUser user;
    Integer eye1, eye2, eye3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);
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
        initUi();
        eye1 = eye2 = eye3 = 0;
        setUiEye(imV_eye1, edt_old_password, 1);
        setUiEye(imV_eye2, edt_new_password, 2);
        setUiEye(imV_eye3, edt_new_password_again, 3);
        setOnClickListener();
    }
    private void initUi(){
        edt_old_password = findViewById(R.id.edt_old_password);
        edt_new_password = findViewById(R.id.edt_new_password);
        edt_new_password_again = findViewById(R.id.edt_new_password_again);
        btn_change_password = findViewById(R.id.btn_change_password);
        imV_back = findViewById(R.id.imV_back);
        imV_eye1 = findViewById(R.id.imV_eye1);
        imV_eye2 = findViewById(R.id.imV_eye2);
        imV_eye3 = findViewById(R.id.imV_eye3);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }
    private void setUiEye(ImageView imv_eye, EditText edt, int x){
        imv_eye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int eye = 0;
                if(x==1) eye = eye1;
                if(x==2) eye = eye2;
                if(x==3) eye = eye3;
                if(eye == 0){
                    // Chuyển icon unhide thành hide
                    imv_eye.setImageResource(R.drawable.ic_hide);
                    // Chuyển text từ hide thành unhide
                    edt.setInputType(InputType.TYPE_CLASS_TEXT);
                    // Đặt con trỏ nháy ở cuối input đã nhập
                    edt.setSelection(edt.getText().length());
                    // Đảo lại trạng thái mắt
                    if(x==1) eye1 = 1;
                    else if(x==2) eye2 = 1;
                    else eye3 = 1;
                }
                else {
                    // Chuyển icon hide thành unhide
                    imv_eye.setImageResource(R.drawable.ic_unhide);
                    // Chuyển text từ unhide thành hide
                    int inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
                    edt.setInputType(inputType);
                    // Đặt con trỏ nháy ở cuối input đã nhập
                    edt.setSelection(edt.getText().length());
                    // Đảo lại trạng thái mắt
                    if(x==1) eye1 = 0;
                    else if(x==2) eye2 = 0;
                    else eye3 = 0;
                }
            }
        });
    }
    private void setOnClickListener(){
        imV_back.setOnClickListener(view -> {
            finish();
            overridePendingTransition(0, 0);
        });
        btn_change_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Gọi đối tượng validate
                Validate validate = new Validate(ChangePassword.this);
                if(!validate.validateChangePassword(getInput(edt_old_password),
                        getInput(edt_new_password), getInput(edt_new_password_again))) return;
                // Check đổi mật khẩu
                reAuthenticateUser();
            }


        });
    }
    private String getInput(EditText edt){
        return edt.getText().toString().trim();
    }

    private void onClickChangePassword(){
        user.updatePassword(getInput(edt_new_password))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            show_dialog("Đổi mật khẩu thành công!", 2);
                            //Back to login
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    auth.signOut();
                                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                    startActivity(intent);
                                    overridePendingTransition(0, 0);
                                    finish();
                                }
                            }, 2000);
                        }
                    }
                });
    }

    private void reAuthenticateUser(){
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), getInput(edt_old_password));
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            if(getInput(edt_old_password).equals(getInput(edt_new_password))){
                                show_dialog("Vui lòng nhập mật khẩu mới khác mật khẩu cũ!", 2);
                                return;
                            }
                            onClickChangePassword();
                        }
                        else{
                            show_dialog("Mật khẩu cũ không đúng. Vui lòng nhập lại!", 2);
                        }
                    }
                });
    }
    // Hàm thông báo dialog
    private void show_dialog(String s, int time){
        ProgressDialog progressDialog = new ProgressDialog(ChangePassword.this);
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
    @Override
    public void onBackPressed() {
        overridePendingTransition(0, 0);
        finish();
    }
}