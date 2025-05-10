package com.vuongnk.appandroid.activity;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.vuongnk.appandroid.R;
import com.vuongnk.appandroid.application.MyApplication;
import com.vuongnk.appandroid.helper.FirebaseAuthHelper;
import com.google.android.material.button.MaterialButton;

import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vuongnk.appandroid.model.Order;

import java.util.ArrayList;
import java.util.List;

public class AdminMainActivity extends AppCompatActivity {
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1;
    private static final String TAG = "AdminMainActivity";

    private Toolbar toolbar;
    private ProgressDialog progressDialog;
    private TextView tvTotalOrders, tvTotalRevenue;
    private MaterialButton btnManageBooks, btnManageOrders, btnManageUsers,
            btnManageFeedback, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION);
            }
        }

        initUI();
        setupToolbar();
        loadStatistics();
        setupListeners();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Quyền nhận thông báo đã được cấp!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Quyền nhận thông báo bị từ chối!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initUI() {
        toolbar = findViewById(R.id.toolbar);
        tvTotalOrders = findViewById(R.id.tv_total_orders);
        tvTotalRevenue = findViewById(R.id.tv_total_revenue);
        btnManageBooks = findViewById(R.id.btn_manage_books);

        btnManageOrders = findViewById(R.id.btn_manage_orders);
        btnManageUsers = findViewById(R.id.btn_manage_users);
        btnManageFeedback = findViewById(R.id.btn_manage_feedback);
        btnLogout = findViewById(R.id.btn_logout);
        progressDialog = new ProgressDialog(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quản lý cửa hàng");
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }
    }

    private void loadStatistics() {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders");
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Order> orders = new ArrayList<>();
                double totalRevenue = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Order order = snapshot.getValue(Order.class);
                    if (order != null) {
                        orders.add(order);
                        if ("COMPLETED".equals(order.getStatus())) {
                            totalRevenue += order.getTotalAmount();
                        }
                    }
                }

                // Cập nhật tổng số đơn hàng
                tvTotalOrders.setText(String.valueOf(orders.size()));

                // Cập nhật tổng doanh thu từ đơn hàng hoàn thành
                tvTotalRevenue.setText(String.format("%,.0fđ", totalRevenue));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi nếu cần
            }
        });
    }

    private void setupListeners() {

        btnManageOrders.setOnClickListener(v -> {
            Intent intent = new Intent(AdminMainActivity.this, ListOrderActivity.class);
            startActivity(intent);
        });

        btnManageUsers.setOnClickListener(v -> {
            Intent intent = new Intent(AdminMainActivity.this, UserManagementActivity.class);
            startActivity(intent);
        });

        btnManageFeedback.setOnClickListener(v -> {
            // TODO: Chuyển đến màn hình quản lý phản hồi
        });

        btnManageBooks.setOnClickListener(v -> {
            Intent intent = new Intent(AdminMainActivity.this, BookManagementActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> handleLogout());
    }

    private void handleLogout() {
        progressDialog.setMessage("Đang đăng xuất...");
        progressDialog.show();

        MyApplication.handleLogout();

        progressDialog.dismiss();

        Intent intent = new Intent(AdminMainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}