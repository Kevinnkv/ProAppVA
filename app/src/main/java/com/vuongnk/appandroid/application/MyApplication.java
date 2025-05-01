package com.vuongnk.appandroid.application;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.util.Log;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vuongnk.appandroid.R;
import com.vuongnk.appandroid.helper.CloudinaryHelper;
import com.vuongnk.appandroid.model.User;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyApplication extends Application {
    private static FirebaseAuth mAuth;
    private static GoogleSignInClient mGoogleSignInClient;
    private static FirebaseUser currentUser;
    private static DatabaseReference usersRef;
    private static User currentUserInfo; // T
    private static Application application;
    private ProgressDialog progressDialog;


    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        progressDialog = new ProgressDialog(this);
        CloudinaryHelper.init(this);
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);

        // Thêm listener để theo dõi thay đổi trạng thái đăng nhập
        mAuth.addAuthStateListener(firebaseAuth -> {
            currentUser = firebaseAuth.getCurrentUser();
            if (currentUser != null) {
                loadUserInfo();
            } else {
                currentUserInfo = null;
            }
        });
    }

    private static void loadUserInfo() {
        if (currentUser == null) {
            currentUserInfo = null;
            return;
        }

        usersRef.child(currentUser.getUid())
                .get()
                .addOnSuccessListener(dataSnapshot -> {
                    currentUserInfo = dataSnapshot.getValue(User.class);
                });
    }

    public static void clearUserInfo() {
        currentUserInfo = null;
        currentUser = null;
    }

    public static User getCurrentUserInfo() {
        if (currentUser == null) {
            currentUser = mAuth.getCurrentUser();
        }
        
        if (currentUser != null && currentUserInfo == null) {
            loadUserInfo();
        }
        
        return currentUserInfo;
    }

    public static void getCurrentUserInfo(UserInfoCallback callback) {
        if (currentUser == null) {
            currentUser = mAuth.getCurrentUser();
        }

        if (currentUser == null) {
            callback.onError("Người dùng chưa đăng nhập");
            return;
        }

        if (currentUserInfo != null) {
            callback.onUserInfoLoaded(currentUserInfo);
            return;
        }

        usersRef.child(currentUser.getUid())
                .get()
                .addOnSuccessListener(dataSnapshot -> {
                    currentUserInfo = dataSnapshot.getValue(User.class);
                    if (currentUserInfo != null) {
                        callback.onUserInfoLoaded(currentUserInfo);
                    } else {
                        callback.onError("Không tìm thấy thông tin người dùng");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public interface UserInfoCallback {
        void onUserInfoLoaded(User user);

        void onError(String errorMessage);
    }

    public interface UpdateCallback {
        void onSuccess();

        void onError(String error);
    }


    public static void updateUserInfo(Map<String, Object> updates, UpdateCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            callback.onError("Người dùng chưa đăng nhập");
            return;
        }

        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUser.getUid())
                .updateChildren(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public static void handleLogout() {
        // Clear user info before signing out
        usersRef.child(currentUser.getUid()).child("token").setValue("");
        clearUserInfo();
        mAuth.signOut();
        mGoogleSignInClient.signOut();

        // Clear login state and user data
        SharedPreferences userPrefs = application.getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userPrefs.edit().clear().apply();
    }

    private static void sendFCMNotification(String fcmToken, String title, String message) {
        String serverKey = "key=YOUR_SERVER_KEY"; // 🔹 Thay YOUR_SERVER_KEY bằng server key Firebase

        try {
            JSONObject json = new JSONObject();
            JSONObject notification = new JSONObject();
            notification.put("title", title);
            notification.put("body", message);

            json.put("to", fcmToken);
            json.put("notification", notification);

            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url("https://fcm.googleapis.com/fcm/send")
                    .post(body)
                    .addHeader("Authorization", serverKey)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("FCM", "Gửi thông báo thất bại: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.d("FCM", "Thông báo gửi thành công: " + response.body().string());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

// Cách 1: Lấy trực tiếp (nếu đã load)
//User user = MyApplication.getCurrentUserInfo();
//        if (user != null) {
//// Sử dụng thông tin user
//String displayName = user.getDisplayName();
//String email = user.getEmail();
//// ...
//        }


// Cách 2: Sử dụng callback (đảm bảo có dữ liệu mới nhất)
// MyApplication.getCurrentUserInfo(new MyApplication.UserInfoCallback() {
//    @Override
//    public void onUserInfoLoaded(User user) {
//        // Sử dụng thông tin user
//        String displayName = user.getDisplayName();
//        String email = user.getEmail();
//        // ...
//    }
//
//    @Override
//    public void onError(String errorMessage) {
//        Toast.makeText(SomeActivity.this,
//                "Lỗi: " + errorMessage,
//                Toast.LENGTH_SHORT).show();
//    }
//});

