package com.vuongnk.appandroid.helper;

import android.content.Context;
import android.util.Log;

import com.vuongnk.appandroid.BuildConfig;
import com.vuongnk.appandroid.model.Notification;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import okhttp3.*;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FCMHelper {
    private static final String TAG = "FCMHelper";
    private static final String FCM_URL = BuildConfig.BASE_URL_SEND_MESSAGE;
    private static final Executor backgroundExecutor = Executors.newSingleThreadExecutor();

    public static void sendNotification(Context context, String deviceToken, String title, String message, String userId) {
        // Lưu thông báo vào Realtime Database
        saveNotification(userId, title, message);

        FirebaseAuthHelper.getAccessToken(context, new FirebaseAuthHelper.AccessTokenCallback() {
            @Override
            public void onTokenReceived(String accessToken) {
                backgroundExecutor.execute(() -> {
                    if (accessToken != null) {
                        sendFCMMessage(accessToken, deviceToken, title, message, userId);
                    } else {
                        Log.e(TAG, "Mã truy cập là null");
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Lỗi khi lấy mã truy cập: " + e.getMessage());
            }
        });
    }

    private static void saveNotification(String userId, String title, String message) {
        try {
            DatabaseReference notificationsRef = FirebaseDatabase.getInstance()
                    .getReference("notifications");

            // Tạo notification mới
            Notification notification = new Notification(userId, title, message);

            // Lưu vào database
            notificationsRef.child(notification.getId())
                    .setValue(notification)
                    .addOnSuccessListener(aVoid ->
                            Log.d(TAG, "Đã lưu thông báo thành công"))
                    .addOnFailureListener(e ->
                            Log.e(TAG, "Lỗi khi lưu thông báo: " + e.getMessage()));

            // Cập nhật danh sách thông báo của user
            DatabaseReference userNotificationsRef = FirebaseDatabase.getInstance()
                    .getReference("user_notifications")
                    .child(userId)
                    .child(notification.getId());

            userNotificationsRef.setValue(true)
                    .addOnFailureListener(e ->
                            Log.e(TAG, "Lỗi khi cập nhật user_notifications: " + e.getMessage()));

        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi lưu thông báo: " + e.getMessage());
        }
    }

    private static void sendFCMMessage(String accessToken, String deviceToken, String title, String message, String userId) {
        OkHttpClient client = new OkHttpClient();

        JSONObject json = new JSONObject();
        try {
            JSONObject notification = new JSONObject();
            notification.put("title", title);
            notification.put("body", message);
            notification.put("userId",userId);

            JSONObject messageObj = new JSONObject();
            messageObj.put("token", deviceToken);
            messageObj.put("notification", notification);

            json.put("message", messageObj);
        } catch (JSONException e) {
            Log.e(TAG, "Lỗi khi tạo JSON: " + e.getMessage());
            return;
        }

        RequestBody body = RequestBody.create(
                MediaType.get("application/json; charset=utf-8"),
                json.toString()
        );

        Request request = new Request.Builder()
                .url(FCM_URL)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try {
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();
            Log.d(TAG, "Phản hồi FCM: " + responseBody);
        } catch (IOException e) {
            Log.e(TAG, "Lỗi FCM: " + e.getMessage());
        }
    }
}