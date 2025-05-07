package com.vuongnk.appandroid.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.vuongnk.appandroid.R;
import com.vuongnk.appandroid.application.MyApplication;
import com.vuongnk.appandroid.helper.UploadHelper;
import com.vuongnk.appandroid.model.User;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imgAvatar;
    private ImageButton btnChangeAvatar;
    private EditText etDisplayName, etPhone, etAddress, et_email, etCity, etState, etStreet, etZipCode;
    private Button btnSave;
    private FirebaseUser currentUser;
    private Uri selectedImageUri;
    private User userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

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
        btnChangeAvatar = findViewById(R.id.btn_change_avatar);
        etDisplayName = findViewById(R.id.et_display_name);
        etPhone = findViewById(R.id.et_phone);
        etAddress = findViewById(R.id.et_address);
        et_email = findViewById(R.id.et_email);
        btnSave = findViewById(R.id.btn_save);
        etCity = findViewById(R.id.et_city);
        etState = findViewById(R.id.et_state);
        etStreet = findViewById(R.id.et_street);
        etZipCode = findViewById(R.id.et_zip_code);
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
        userInfo = MyApplication.getCurrentUserInfo();
        if (userInfo != null) {
            etDisplayName.setText(userInfo.getDisplayName() != null ? userInfo.getDisplayName() : "");
            et_email.setText(userInfo.getEmail() != null ? userInfo.getEmail() : "");
            etPhone.setText(userInfo.getPhoneNumber() != null ? userInfo.getPhoneNumber() : "");

            // Hiển thị thông tin địa chỉ chi tiết
            if (userInfo.getAddress() != null) {
                etAddress.setText(userInfo.getAddress().getCountry() != null ? userInfo.getAddress().getCountry() : "");
                etCity.setText(userInfo.getAddress().getCity() != null ? userInfo.getAddress().getCity() : "");
                etState.setText(userInfo.getAddress().getState() != null ? userInfo.getAddress().getState() : "");
                etStreet.setText(userInfo.getAddress().getStreet() != null ? userInfo.getAddress().getStreet() : "");
                etZipCode.setText(userInfo.getAddress().getZipCode() != null ? userInfo.getAddress().getZipCode() : "");
            }

            if (userInfo.getPhotoURL() != null && !userInfo.getPhotoURL().isEmpty()) {
                Glide.with(EditProfileActivity.this)
                        .load(userInfo.getPhotoURL())
                        .placeholder(R.drawable.baseline_person_24)
                        .error(R.drawable.placeholder_book)
                        .into(imgAvatar);
            }
            setupListeners();
        }
    }

    private void setupListeners() {
        btnChangeAvatar.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), PICK_IMAGE_REQUEST);
        });

        btnSave.setOnClickListener(v -> saveUserInfo());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                imgAvatar.setImageURI(selectedImageUri);
            }
        }
    }

    private void saveUserInfo() {
        String displayName = etDisplayName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // Lấy thông tin địa chỉ
        Map<String, String> address = new HashMap<>();
        address.put("country", etAddress.getText().toString().trim());
        address.put("city", etCity.getText().toString().trim());
        address.put("state", etState.getText().toString().trim());
        address.put("street", etStreet.getText().toString().trim());
        address.put("zipCode", etZipCode.getText().toString().trim());

        // Kiểm tra dữ liệu
        if (TextUtils.isEmpty(displayName)) {
            etDisplayName.setError("Vui lòng nhập họ và tên");
            return;
        }

        final Map<String, Object> updates = new HashMap<>();
        updates.put("displayName", displayName);
        updates.put("phoneNumber", phone);
        updates.put("address", address);
        updates.put("updatedAt", System.currentTimeMillis());

        if (selectedImageUri != null) {
            // Hiển thị thông báo đang xử lý
            Toast.makeText(EditProfileActivity.this,
                    "Đang xử lý, vui lòng đợi...",
                    Toast.LENGTH_SHORT).show();

            // Upload ảnh sử dụng UploadHelper
            UploadHelper.uploadImage(
                    selectedImageUri,
                    "avatars",
                    "user_" + currentUser.getUid(),
                    new UploadHelper.UploadCallbackListener() {
                        @Override
                        public void onSuccess(String imageUrl) {
                            // Cập nhật URL ảnh vào Firebase Database
                            updates.put("photoURL", imageUrl);
                            updateUserProfile(updates);
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> Toast.makeText(EditProfileActivity.this,
                                    "Lỗi khi tải ảnh lên: " + error,
                                    Toast.LENGTH_SHORT).show());
                        }
                    }
            );
        } else {
            // Cập nhật thông tin không có ảnh
            updateUserProfile(updates);
        }
    }

    private void updateUserProfile(Map<String, Object> updates) {
        MyApplication.updateUserInfo(updates, new MyApplication.UpdateCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(EditProfileActivity.this,
                        "Cập nhật thông tin thành công",
                        Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(EditProfileActivity.this,
                        "Lỗi khi cập nhật thông tin: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
