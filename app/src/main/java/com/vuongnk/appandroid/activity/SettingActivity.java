package com.vuongnk.appandroid.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.vuongnk.appandroid.R;
import com.vuongnk.appandroid.application.MyApplication;
import com.vuongnk.appandroid.model.User;
import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity {
    private CircleImageView imgAvatar;
    private TextView tvDisplayName, tvEmail, tvAccountType, tvAccountBalance;
    private Button btnEditProfile, btnChangePassword;
    private TextInputEditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private FirebaseUser currentUser;
    private User userInfo;
    private String providerType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }

        initUI();
        setupToolbar();
        loadUserInfo();
    }

    private void initUI() {
        imgAvatar = findViewById(R.id.img_avatar);
        tvDisplayName = findViewById(R.id.tv_display_name);
        tvEmail = findViewById(R.id.tv_email);
        tvAccountType = findViewById(R.id.tv_account_type);
        tvAccountBalance = findViewById(R.id.tv_accountBalance);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnChangePassword = findViewById(R.id.btn_change_password);
        etCurrentPassword = findViewById(R.id.et_current_password);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadUserInfo() {
        MyApplication.getCurrentUserInfo(new MyApplication.UserInfoCallback() {
            @Override
            public void onUserInfoLoaded(User user) {
                userInfo = user;

                // Xác định loại tài khoản
                determineProviderType();

                // Hiển thị thông tin người dùng
                tvDisplayName.setText(userInfo.getDisplayName() != null ? userInfo.getDisplayName() : "");
                tvEmail.setText(userInfo.getEmail() != null ? userInfo.getEmail() : "");
                tvAccountType.setText(providerType);
                tvAccountBalance.setText("Số dư: " + userInfo.getAccountBalance());

                // Tải ảnh đại diện
                if (userInfo.getPhotoURL() != null && !userInfo.getPhotoURL().isEmpty()) {
                    Glide.with(SettingActivity.this)
                            .load(userInfo.getPhotoURL())
                            .placeholder(R.drawable.baseline_person_24)
                            .error(R.drawable.placeholder_book)
                            .into(imgAvatar);
                }

                // Thiết lập trạng thái nút đổi mật khẩu
                setupPasswordChangeUI();
                setupListeners();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(SettingActivity.this,
                        "Lỗi tải thông tin: " + errorMessage,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void determineProviderType() {
        // Kiểm tra loại xác thực
        for (com.google.firebase.auth.UserInfo profile : currentUser.getProviderData()) {
            if (profile.getProviderId().equals(EmailAuthProvider.PROVIDER_ID)) {
                providerType = "Email/Mật khẩu";
                return;
            } else if (profile.getProviderId().equals(GoogleAuthProvider.PROVIDER_ID)) {
                providerType = "Google Sign-In";
                return;
            }
        }
        providerType = "Không xác định";
    }

    private void setupPasswordChangeUI() {
        boolean isEmailPasswordAccount = "Email/Mật khẩu".equals(providerType);

        btnChangePassword.setEnabled(isEmailPasswordAccount);
        etCurrentPassword.setEnabled(isEmailPasswordAccount);
        etNewPassword.setEnabled(isEmailPasswordAccount);
        etConfirmPassword.setEnabled(isEmailPasswordAccount);

        if (!isEmailPasswordAccount) {
            btnChangePassword.setText("Không thể đổi mật khẩu");
            etCurrentPassword.setHint("Chỉ dành cho tài khoản Email");
            etNewPassword.setHint("Chỉ dành cho tài khoản Email");
            etConfirmPassword.setHint("Chỉ dành cho tài khoản Email");
        } else {
            btnChangePassword.setText("Xác nhận");
            etCurrentPassword.setHint("Nhập mật khẩu hiện tại");
            etNewPassword.setHint("Nhập mật khẩu mới");
            etConfirmPassword.setHint("Xác nhận mật khẩu mới");
            btnChangePassword.setVisibility(View.VISIBLE);
        }
    }

    private void setupListeners() {
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(SettingActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        btnChangePassword.setOnClickListener(v -> changePassword());
    }

    private void changePassword() {
        // Kiểm tra lại loại tài khoản
        if (!"Email/Mật khẩu".equals(providerType)) {
            Toast.makeText(this, "Chức năng chỉ dành cho tài khoản Email", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Kiểm tra đầu vào
        if (TextUtils.isEmpty(currentPassword) ||
                TextUtils.isEmpty(newPassword) ||
                TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu mới không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hiển thị dialog loading
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang cập nhật mật khẩu...");
        progressDialog.show();

        // Xác thực lại người dùng
        AuthCredential credential = EmailAuthProvider
                .getCredential(currentUser.getEmail(), currentPassword);

        final Map<String, Object> updates = new HashMap<>();
        updates.put("password", newPassword);


        currentUser.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {
                    // Cập nhật mật khẩu mới
                    updateUserProfile(updates);
                    currentUser.updatePassword(newPassword)
                            .addOnSuccessListener(aVoid1 -> {
                                progressDialog.dismiss();
                                Toast.makeText(SettingActivity.this,
                                        "Đổi mật khẩu thành công",
                                        Toast.LENGTH_SHORT).show();

                                // Xóa các trường nhập
                                etCurrentPassword.setText("");
                                etNewPassword.setText("");
                                etConfirmPassword.setText("");
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(SettingActivity.this,
                                        "Lỗi khi đổi mật khẩu: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(SettingActivity.this,
                            "Mật khẩu hiện tại không đúng",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUserProfile(Map<String, Object> updates) {
        MyApplication.updateUserInfo(updates, new MyApplication.UpdateCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(SettingActivity.this,
                        "Cập nhật thông tin thành công",
                        Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(SettingActivity.this,
                        "Lỗi khi cập nhật thông tin: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
