package com.vuongnk.appandroid.fragment;


import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.vuongnk.appandroid.R;
import com.vuongnk.appandroid.activity.AdminMainActivity;
import com.vuongnk.appandroid.activity.MainActivity;
import com.vuongnk.appandroid.dialog.CustomDialogFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class LoginFragment extends Fragment {
    private static final String TAG = "LoginFragment";
    private static final String PREF_NAME = "UserPrefs";
    private static final String ROLE_KEY = "role";

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private SignInButton btnGoogleSignIn;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> signInLauncher;
    private TextView tvAdminEmail, tvHotline;
    private String token;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        initializeComponents(view);
        setupGoogleSignIn();
        setupClickListeners();

        return view;
    }

    private void initializeComponents(View view) {
        mAuth = FirebaseAuth.getInstance();
        fetchFirebaseToken();

        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        btnLogin = view.findViewById(R.id.btn_login);
        btnGoogleSignIn = view.findViewById(R.id.btn_google_sign_in);
        progressBar = view.findViewById(R.id.progress_bar);
        tvAdminEmail = view.findViewById(R.id.tv_admin_email);
        tvHotline = view.findViewById(R.id.tv_hotline);

        for (int i = 0; i < btnGoogleSignIn.getChildCount(); i++) {
            View v = btnGoogleSignIn.getChildAt(i);
            if (v instanceof TextView) {
                ((TextView) v).setText("Đăng nhập bằng tài khoản Google");
                break;
            }
        }
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    handleGoogleSignInResult(task);
                });
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> loginWithEmailPassword());
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());

        setupContactListeners();
    }

    private void setupContactListeners() {
        tvAdminEmail.setOnClickListener(v -> sendSupportEmail());
        tvHotline.setOnClickListener(v -> dialSupportHotline());
    }

    private void sendSupportEmail() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"admin@bansach.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Hỗ trợ tài khoản");

        try {
            startActivity(Intent.createChooser(intent, "Gửi email..."));
        } catch (android.content.ActivityNotFoundException ex) {
            showToast("Không tìm thấy ứng dụng email.");
        }
    }

    private void dialSupportHotline() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:1900xxxx"));

        try {
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException ex) {
            showToast("Không thể thực hiện cuộc gọi.");
        }
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

    private void signInWithGoogle() {
        if (checkGooglePlayServices()) {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            signInLauncher.launch(signInIntent);
        }
    }

    private boolean checkGooglePlayServices() {
        int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(requireContext());
        if (status != ConnectionResult.SUCCESS) {
            if (GoogleApiAvailability.getInstance().isUserResolvableError(status)) {
                GoogleApiAvailability.getInstance().getErrorDialog(
                        requireActivity(),
                        status,
                        2404
                ).show();
            } else {
                showErrorDialog("Lỗi", "Thiết bị không hỗ trợ Google Play Services");
            }
            return false;
        }
        return true;
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null && account.getIdToken() != null) {
                firebaseAuthWithGoogle(account.getIdToken());
            } else {
                showErrorDialog("Đăng nhập thất bại", "Không thể lấy thông tin tài khoản Google");
            }
        } catch (ApiException e) {
            // Log chi tiết lỗi
            Log.e(TAG, "Google sign in failed", e);

            // Hiển thị thông báo lỗi chi tiết hơn
            String errorMessage = "Lỗi đăng nhập: ";
            switch (e.getStatusCode()) {
                case GoogleSignInStatusCodes.SIGN_IN_CANCELLED:
                    errorMessage += "Đăng nhập bị hủy";
                    break;
                case GoogleSignInStatusCodes.SIGN_IN_FAILED:
                    errorMessage += "Đăng nhập không thành công";
                    break;
                case GoogleSignInStatusCodes.NETWORK_ERROR:
                    errorMessage += "Lỗi mạng";
                    break;
                default:
                    errorMessage += e.getMessage();
            }

            showErrorDialog("Đăng nhập thất bại", errorMessage);
        }
    }


    private void firebaseAuthWithGoogle(String idToken) {
        setLoadingState(true);

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            checkAndCreateUserProfile(firebaseUser);
                        } else {
                            setLoadingState(false);
                            showErrorDialog("Lỗi", "Không thể lấy thông tin người dùng");
                        }
                    } else {
                        setLoadingState(false);
                        // Xử lý chi tiết các loại lỗi xác thực
                        Exception exception = task.getException();
                        String errorMessage = "Xác thực thất bại: ";

                        if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                            errorMessage += "Thông tin đăng nhập không hợp lệ";
                        } else {
                            errorMessage += exception.getMessage();
                        }

                        showErrorDialog("Lỗi Xác Thực", errorMessage);
                    }
                });
    }

    private void checkAndCreateUserProfile(FirebaseUser firebaseUser) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        AtomicBoolean isProcessed = new AtomicBoolean(false);

        usersRef.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (isProcessed.getAndSet(true)) return;

                setLoadingState(false);

                if (!dataSnapshot.exists()) {
                    createNewUserProfile(firebaseUser);
                } else {
                    processExistingUserProfile(dataSnapshot, firebaseUser);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (isProcessed.getAndSet(true)) return;

                setLoadingState(false);
                showToast("Lỗi truy vấn cơ sở dữ liệu: " + databaseError.getMessage());
            }
        });
    }

    private void processExistingUserProfile(DataSnapshot dataSnapshot, FirebaseUser firebaseUser) {
        Integer isActive = dataSnapshot.child("isActive").getValue(Integer.class);
        if (isActive != null && isActive == 0) {
            showAccountLockedDialog();
            mAuth.signOut();
            mGoogleSignInClient.signOut();
            return;
        }

        String role = dataSnapshot.child("role").getValue(String.class);
        updateUserToken(firebaseUser.getUid());
        saveUserRole(role);
        navigateToMainActivity(role);
    }

    private void updateUserToken(String uid) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        if (token != null) {
            usersRef.child(uid).child("token").setValue(token);
        }
    }

    private void createNewUserProfile(FirebaseUser firebaseUser) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        Map<String, Object> userMap = createUserMap(firebaseUser);

        usersRef.child(firebaseUser.getUid()).setValue(userMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        saveUserRole("user");
                        navigateToMainActivity("user");
                    } else {
                        showToast("Không thể tạo hồ sơ người dùng: " + task.getException().getMessage());
                    }
                });
    }

    private Map<String, Object> createUserMap(FirebaseUser firebaseUser) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("uid", firebaseUser.getUid());
        userMap.put("email", firebaseUser.getEmail());
        userMap.put("displayName", firebaseUser.getDisplayName() != null ?
                firebaseUser.getDisplayName() : "Người dùng mới");
        userMap.put("photoURL", firebaseUser.getPhotoUrl() != null ?
                firebaseUser.getPhotoUrl().toString() : "");
        userMap.put("phoneNumber", firebaseUser.getPhoneNumber() != null ?
                firebaseUser.getPhoneNumber() : "");
        userMap.put("isActive", 1);
        userMap.put("role", "user");
        userMap.put("accountBalance", 0);
        userMap.put("token", token);
        userMap.put("createdAt", System.currentTimeMillis());
        userMap.put("updatedAt", System.currentTimeMillis());

        userMap.put("address", createDefaultAddressMap());
        return userMap;
    }

    private Map<String, Object> createDefaultAddressMap() {
        Map<String, Object> addressMap = new HashMap<>();
        addressMap.put("street", "");
        addressMap.put("city", "");
        addressMap.put("state", "");
        addressMap.put("zipCode", "");
        addressMap.put("country", "Việt Nam");
        return addressMap;
    }

    private void setLoadingState(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!isLoading);
        btnGoogleSignIn.setEnabled(!isLoading);
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
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