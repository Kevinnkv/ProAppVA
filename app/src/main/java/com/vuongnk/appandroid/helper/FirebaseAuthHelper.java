package com.vuongnk.appandroid.helper;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Handler;
import android.os.Looper;

import com.google.auth.oauth2.GoogleCredentials;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FirebaseAuthHelper {
    private static final String TAG = "FirebaseAuthHelper";
    private static final String FIREBASE_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    // Interface callback để xử lý token
    public interface AccessTokenCallback {
        void onTokenReceived(String token);
        void onError(Exception e);
    }

    // Phương thức lấy access token từ service account (bất đồng bộ)
    public static void getAccessToken(Context context, AccessTokenCallback callback) {
        executorService.execute(() -> {
            try {
                AssetManager assetManager = context.getAssets();
                InputStream serviceAccount = assetManager.open("service-account.json");

                GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount)
                        .createScoped(FIREBASE_SCOPE);
                credentials.refreshIfExpired();

                // Sử dụng Handler để trả về trên luồng chính
                new Handler(Looper.getMainLooper()).post(() ->
                        callback.onTokenReceived(credentials.getAccessToken().getTokenValue())
                );
            } catch (IOException e) {
                // Trả về lỗi trên luồng chính
                new Handler(Looper.getMainLooper()).post(() ->
                        callback.onError(e)
                );
            }
        });
    }

    // Phương thức lấy token đồng bộ (để sử dụng trong các trường hợp cần thiết)
    public static String getAccessTokenSync(Context context) throws Exception {
        AssetManager assetManager = context.getAssets();
        InputStream serviceAccount = assetManager.open("service-account.json");

        GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount)
                .createScoped(FIREBASE_SCOPE);
        credentials.refreshIfExpired();

        return credentials.getAccessToken().getTokenValue();
    }
}