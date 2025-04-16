package com.vuongnk.appandroid.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.vuongnk.appandroid.R;
import com.vuongnk.appandroid.activity.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.vuongnk.appandroid.model.User;

public class RegisterFragment extends Fragment {
    private EditText etName, etEmail, etPassword, etConfirmPassword, etPhone;
    private Button btnRegister;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private static final String TAG = "RegisterFragment";
    public String token;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        getToken();

        // Ánh xạ view
        etName = view.findViewById(R.id.et_name);
        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        etConfirmPassword = view.findViewById(R.id.et_confirm_password);
        etPhone = view.findViewById(R.id.et_phone);
        btnRegister = view.findViewById(R.id.btn_register);
        progressBar = view.findViewById(R.id.progress_bar);

        btnRegister.setOnClickListener(v -> validateAndRegister());

        return view;
    }

    private void validateAndRegister() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // Kiểm tra dữ liệu
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() ||
                confirmPassword.isEmpty() || phone.isEmpty()) {
            showError("Vui lòng điền đầy đủ thông tin");
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu không khớp");
            return;
        }

        showLoading(true);

        // Kiểm tra email và số điện thoại đã tồn tại chưa
        checkExistingUserData(email, phone);
    }

    private void checkExistingUserData(String email, String phone) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Kiểm tra email
        Query emailQuery = usersRef.orderByChild("email").equalTo(email);
        emailQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Email đã tồn tại
                    showLoading(false);
                    etEmail.setError("Email này đã được sử dụng");
                    showError("Email này đã được sử dụng");
                } else {
                    // Kiểm tra số điện thoại
                    checkExistingPhone(phone);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showLoading(false);
                showError("Lỗi kiểm tra dữ liệu: " + databaseError.getMessage());
                Log.e(TAG, "Lỗi kiểm tra email: ", databaseError.toException());
            }
        });
    }

    private void checkExistingPhone(String phone) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Kiểm tra số điện thoại
        Query phoneQuery = usersRef.orderByChild("phoneNumber").equalTo(phone);
        phoneQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Số điện thoại đã tồn tại
                    showLoading(false);
                    etPhone.setError("Số điện thoại này đã được sử dụng");
                    showError("Số điện thoại này đã được sử dụng");
                } else {
                    // Cả email và số điện thoại đều chưa được sử dụng
                    // Tiến hành đăng ký
                    registerUser();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showLoading(false);
                showError("Lỗi kiểm tra dữ liệu: " + databaseError.getMessage());
                Log.e(TAG, "Lỗi kiểm tra số điện thoại: ", databaseError.toException());
            }
        });
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        saveUserData(firebaseUser.getUid(), name, email, phone, password);
                    } else {
                        showLoading(false);
                        showError("Đăng ký thất bại: " + task.getException().getMessage());
                        Log.e(TAG, "Đăng ký thất bại: ", task.getException());
                    }
                });
    }

    private void saveUserData(String userId, String name, String email, String phone, String password) {
        // Tạo đối tượng User mới
        User user = new User(name, email, phone, password);
        user.setUid(userId);
        user.setDisplayName(name);
        user.setRole("user");
        user.setIsActive(1);
        user.setAccountBalance(0);
        user.setPhotoURL("");
        user.setToken(token);
        user.setCreatedAt(System.currentTimeMillis());
        user.setUpdatedAt(System.currentTimeMillis());

        // Tạo địa chỉ mặc định
        User.Address address = new User.Address("", "Việt Nam", "", "", "");
        user.setAddress(address);

        // Lưu vào Realtime Database
        mDatabase.child("users").child(userId).setValue(user)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    Toast.makeText(requireContext(), "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    saveInfoUser();
                    startMainActivity();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    showError("Lưu thông tin thất bại: " + e.getMessage());
                    Log.e(TAG, "Lưu thông tin thất bại: ", e);
                });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!isLoading);
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void startMainActivity() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void saveInfoUser() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        sharedPreferences.edit().putString("role", "user").apply();
    }

    public void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (task.isSuccessful()) {
                    token = task.getResult();
                } else {
                    Exception exception = task.getException();
                    if (exception != null) {
                        exception.printStackTrace();
                    }
                }
            }
        });
    }
}