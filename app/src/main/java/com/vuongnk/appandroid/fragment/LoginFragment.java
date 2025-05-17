package com.vuongnk.appandroid.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.vuongnk.appandroid.R;
import com.vuongnk.appandroid.activity.AdminMainActivity;
import com.vuongnk.appandroid.activity.MainActivity;
import com.vuongnk.appandroid.dialog.CustomDialogFragment;


public class LoginFragment extends Fragment {
    private static final String TAG = "LoginFragment";
    private static final String PREF_NAME = "UserPrefs";
    private static final String ROLE_KEY = "role";

    private EditText etEmail, etPassword;
    private Button btnLogin;

    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private String token;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        initializeComponents(view);
        setupClickListeners();

        return view;
    }

    private void initializeComponents(View view) {
        mAuth = FirebaseAuth.getInstance();
        fetchFirebaseToken();

        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        btnLogin = view.findViewById(R.id.btn_login);
        progressBar = view.findViewById(R.id.progress_bar);
    }



    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> loginWithEmailPassword());
    }

    private void loginWithEmailPassword() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate input using TextUtils instead of manual checks
        if (TextUtils.isEmpty(email)) {
            showErrorDialog("Thông báo", "Vui lòng nhập email");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            showErrorDialog("Thông báo", "Vui lòng nhập mật khẩu");
            return;
        }

        performEmailPasswordLogin(email, password);
    }

    private void performEmailPasswordLogin(String email, String password) {
        setLoadingState(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    setLoadingState(false);

                    if (task.isSuccessful()) {
                        checkUserRoleAndNavigate();
                    } else {
                        handleLoginError(task.getException());
                    }
                });
    }

    private void handleLoginError(Exception exception) {
        String errorMessage;
        if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            errorMessage = "Email hoặc mật khẩu không chính xác";
        } else if (exception instanceof FirebaseAuthInvalidUserException) {
            errorMessage = "Tài khoản không tồn tại";
        } else {
            errorMessage = "Đăng nhập thất bại: " + exception.getMessage();
        }
        showErrorDialog("Thông báo", errorMessage);
    }

    private void checkUserRoleAndNavigate() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUser.getUid());

        userRef.get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                checkAccountStatusAndNavigate(dataSnapshot);
            }
        }).addOnFailureListener(e ->
                showErrorDialog("Thông báo", "Không thể kiểm tra thông tin người dùng")
        );
    }

    private void checkAccountStatusAndNavigate(DataSnapshot dataSnapshot) {
        Integer isActive = dataSnapshot.child("isActive").getValue(Integer.class);
        if (isActive != null && isActive == 0) {
            showAccountLockedDialog();
            mAuth.signOut();
            return;
        }

        String role = dataSnapshot.child("role").getValue(String.class);
        saveUserRole(role);
        navigateToMainActivity(role);
    }

    private void saveUserRole(String role) {
        SharedPreferences sharedPreferences = requireActivity()
                .getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        sharedPreferences.edit().putString(ROLE_KEY, role).apply();
    }

    private void navigateToMainActivity(String role) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && token != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(currentUser.getUid());

            userRef.child("token").setValue(token);
        }

        // Determine target activity based on role
        Class<?> targetActivity = "user".equals(role) ?
                MainActivity.class : AdminMainActivity.class;

        Intent intent = new Intent(requireActivity(), targetActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    private void setLoadingState(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!isLoading);
        //btnGoogleSignIn.setEnabled(!isLoading);
    }

    private void fetchFirebaseToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        token = task.getResult();
                    }
                });
    }

    private void showErrorDialog(String title, String message) {
        CustomDialogFragment errorDialog = CustomDialogFragment.newInstance(
                CustomDialogFragment.DialogType.ERROR,
                title,
                message,
                "Thử lại"
        );
        errorDialog.setOnActionClickListener(() -> {
            etPassword.setText("");
            etPassword.requestFocus();
        });
        errorDialog.show(getParentFragmentManager(), CustomDialogFragment.TAG);
    }

    private void showAccountLockedDialog() {
        CustomDialogFragment errorDialog = CustomDialogFragment.newInstance(
                CustomDialogFragment.DialogType.ERROR,
                "Thông báo",
                "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ admin qua:\n\n" +
                        "Email: admin@bansach.com\n" +
                        "Hotline: 1900 xxxx\n\n" +
                        "để được hỗ trợ.",
                "Đóng"
        );
        errorDialog.show(getParentFragmentManager(), CustomDialogFragment.TAG);
    }
}