package com.vuongnk.appandroid.activity;


import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vuongnk.appandroid.R;
import com.vuongnk.appandroid.adapter.OrderDetailAdapter;
import com.vuongnk.appandroid.dialog.CustomDialogFragment;
import com.vuongnk.appandroid.model.Order;
import com.vuongnk.appandroid.model.OrderDetail;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderDetailActivity extends AppCompatActivity {
    private TextView tvOrderId, tvOrderStatus, tvOrderDate, tvAddress, tvPhone, tvName, tvTotalAmount;
    private RecyclerView rcvOrderItems;
    private Button btnCancelOrder;
    private View layoutCancelReason;
    private EditText etCancelReason;
    private Order order;
    private DatabaseReference orderRef;
    private List<OrderDetail> orderDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        // Nhận order từ intent
        order = (Order) getIntent().getSerializableExtra("order");
        if (order == null) {
            finish();
            return;
        }

        initViews();
        setupToolbar();
        loadOrderDetails();
        displayOrderInfo();
        setupCancelButton();
    }

    private void initViews() {
        tvOrderId = findViewById(R.id.tvOrderId);
        tvOrderStatus = findViewById(R.id.tvOrderStatus);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvAddress = findViewById(R.id.tvAddress);
        tvPhone = findViewById(R.id.tvPhone);
        tvName = findViewById(R.id.tvName);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        rcvOrderItems = findViewById(R.id.rcvOrderItems);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);
        layoutCancelReason = findViewById(R.id.layoutCancelReason);
        etCancelReason = findViewById(R.id.etCancelReason);

        orderRef = FirebaseDatabase.getInstance().getReference("orders").child(order.getId());
        orderDetails = new ArrayList<>();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết đơn hàng");
        }
    }

    private void displayOrderInfo() {
        tvOrderId.setText("Mã đơn: #" + order.getId());
        tvOrderStatus.setText("Trạng thái: " + getStatusText(order.getStatus()));
        tvOrderDate.setText("Ngày đặt: " + new SimpleDateFormat("dd/MM/yyyy HH:mm")
                .format(new Date(order.getCreatedAt())));
        tvName.setText("Họ tên: " + order.getName());
        tvAddress.setText("Địa chỉ: " + order.getAddress());
        tvPhone.setText("Số điện thoại: " + order.getPhoneNumber());
        tvTotalAmount.setText("Tổng tiền: " +
                new DecimalFormat("#,###").format(order.getTotalAmount()) + "đ");

        // Hiển thị nút hủy đơn nếu đơn hàng đang ở trạng thái chờ xử lý
        if ("PENDING".equals(order.getStatus())) {
            btnCancelOrder.setVisibility(View.VISIBLE);
        } else {
            btnCancelOrder.setVisibility(View.GONE);
        }
    }

    private void loadOrderDetails() {
        DatabaseReference orderDetailsRef = FirebaseDatabase.getInstance()
                .getReference("orderDetails");

        orderDetailsRef.orderByChild("orderId").equalTo(order.getId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orderDetails.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            OrderDetail detail = dataSnapshot.getValue(OrderDetail.class);
                            if (detail != null) {
                                orderDetails.add(detail);
                            }
                        }
                        displayOrderDetails();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(OrderDetailActivity.this,
                                "Lỗi: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displayOrderDetails() {
        OrderDetailAdapter adapter = new OrderDetailAdapter(this, orderDetails);
        rcvOrderItems.setAdapter(adapter);
        rcvOrderItems.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupCancelButton() {
        btnCancelOrder.setOnClickListener(v -> {
            if (layoutCancelReason.getVisibility() == View.VISIBLE) {
                // Nếu đã hiện form nhập lý do
                String reason = etCancelReason.getText().toString().trim();
                if (reason.isEmpty()) {
                    etCancelReason.setError("Vui lòng nhập lý do hủy đơn");
                    return;
                }
                showConfirmCancelDialog(reason);
            } else {
                // Hiện form nhập lý do
                layoutCancelReason.setVisibility(View.VISIBLE);
                btnCancelOrder.setText("Xác nhận hủy đơn");
            }
        });
    }

    private void showConfirmCancelDialog(String reason) {
        List<String> buttonTexts = Arrays.asList("Đồng Ý", "Hủy");
        CustomDialogFragment dialogMulti = CustomDialogFragment.newInstanceMultiButton(
                CustomDialogFragment.DialogType.WARNING,
                "Xác nhận hủy đơn",
                "Bạn có chắc chắn muốn hủy đơn hàng này?",
                buttonTexts
        );
        dialogMulti.setOnMultiButtonClickListener(buttonIndex -> {
            switch (buttonIndex) {
                case 0:
                    cancelOrder(reason);
                    break;
                case 1:
                    dialogMulti.dismiss();
                    break;
            }
            ;
        });

        dialogMulti.show(getSupportFragmentManager(), CustomDialogFragment.TAG);
    }

    private void cancelOrder(String reason) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "CANCELED");
        updates.put("cancelReason", reason);
        updates.put("cancelledAt", System.currentTimeMillis());

        orderRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã hủy đơn hàng", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this,
                        "Lỗi: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
    }

    private String getStatusText(String status) {
        switch (status) {
            case "PENDING":
                return "Chờ xử lý";
            case "SHIPPING":
                return "Đang giao";
            case "COMPLETED":
                return "Hoàn thành";
            case "CANCELED":
                return "Đã hủy";
            default:
                return status;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
