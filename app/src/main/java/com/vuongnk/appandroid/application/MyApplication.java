package com.vuongnk.appandroid.application;


import android.app.Application;
import android.app.ProgressDialog;
import android.util.Log;

import com.vuongnk.appandroid.model.User;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

//import okhttp3.Call;
//import okhttp3.Callback;
//import okhttp3.MediaType;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.RequestBody;
//import okhttp3.Response;

public class MyApplication extends Application {
    private static FirebaseAuth mAuth;
   // private static GoogleSignInClient mGoogleSignInClient;
    private static FirebaseUser currentUser;
    private static DatabaseReference usersRef;
    private static User currentUserInfo; // T
    private static Application application;
    private ProgressDialog progressDialog;


    @Override
    public void onCreate() {
        super.onCreate();
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
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
                .addOnSuccessListener(snapshot -> currentUserInfo = snapshot.getValue(User.class));
    }

    public static void clearUserInfo() {
        currentUser = null;
        currentUserInfo = null;
    }

    public static void handleLogout() {
        if (currentUser != null) {
            usersRef.child(currentUser.getUid())
                    .child("token")
                    .setValue("");
        }
        clearUserInfo();
        FirebaseAuth.getInstance().signOut();
    }

    // lấy thông tin người dùng
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
    // thông báo khi vào đăng nhập -> Ánh làm

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


