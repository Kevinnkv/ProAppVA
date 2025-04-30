package com.vuongnk.appandroid.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.vuongnk.appandroid.R;
import com.vuongnk.appandroid.application.MyApplication;
import com.vuongnk.appandroid.model.Feedback;
import com.vuongnk.appandroid.model.User;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.app.ProgressDialog;

public class ContactActivity extends AppCompatActivity {

    private TextView tvPhone, tvEmail;
    private TextInputEditText etSubject, etMessage;
    private Button btnSend;
    private DatabaseReference feedbackRef;
    private FirebaseUser currentUser;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        // Khởi tạo Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        feedbackRef = FirebaseDatabase.getInstance().getReference("feedbacks");

        // Khởi tạo ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang gửi phản hồi...");

        initUI();
        setupToolbar();
        setupListeners();
    }

    private void initUI() {
        tvPhone = findViewById(R.id.tv_phone);
        tvEmail = findViewById(R.id.tv_email);
        etSubject = findViewById(R.id.et_subject);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
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

    private void setupListeners() {
        // Xử lý click số điện thoại
        tvPhone.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + tvPhone.getText().toString()));
            startActivity(intent);
        });

        // Xử lý gửi phản hồi
        btnSend.setOnClickListener(v -> sendFeedback());
    }

    private void sendFeedback() {
        String subject = etSubject.getText().toString().trim();
        String message = etMessage.getText().toString().trim();

        if (TextUtils.isEmpty(subject)) {
            etSubject.setError("Vui lòng nhập tiêu đề");
            return;
        }

        if (TextUtils.isEmpty(message)) {
            etMessage.setError("Vui lòng nhập nội dung");
            return;
        }

        progressDialog.show();

        // Lấy thông tin user từ MyApplication
        User user = MyApplication.getCurrentUserInfo();
        String userName = user != null ? user.getDisplayName() : currentUser.getDisplayName();

        // Tạo ID mới cho feedback
        String feedbackId = feedbackRef.push().getKey();
        if (feedbackId == null) {
            progressDialog.dismiss();
            Toast.makeText(this, "Lỗi: Không thể tạo ID phản hồi", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo đối tượng Feedback
        Feedback feedback = new Feedback(
                feedbackId,
                currentUser.getUid(),
                userName,
                subject,
                message,
                System.currentTimeMillis(),
                "pending" // Trạng thái chờ xử lý
        );

        // Lưu vào Realtime Database
        feedbackRef.child(feedbackId)
                .setValue(feedback)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();

                    // Gửi email
//                sendEmail(subject, message);

                    // Xóa nội dung đã nhập
                    etSubject.setText("");
                    etMessage.setText("");

                    Toast.makeText(ContactActivity.this,
                            "Đã gửi phản hồi thành công",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(ContactActivity.this,
                            "Lỗi: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void sendEmail(String subject, String message) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + tvEmail.getText().toString()));
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, message);

        try {
            startActivity(Intent.createChooser(intent, "Gửi email..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this,
                    "Không tìm thấy ứng dụng email nào",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}