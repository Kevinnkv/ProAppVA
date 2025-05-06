package com.vuongnk.appandroid.activity;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.vuongnk.appandroid.R;
import com.vuongnk.appandroid.application.MyApplication;
import com.vuongnk.appandroid.dialog.CustomDialogFragment;
import com.vuongnk.appandroid.model.Book;
import com.vuongnk.appandroid.model.CartItem;
import com.vuongnk.appandroid.model.Order;
import com.vuongnk.appandroid.model.OrderDetail;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CheckoutActivity extends AppCompatActivity {
    private EditText etAddress, etPhone, etName;
    private RadioGroup rgPaymentMethod;
    private RadioButton rbOnline, rbCOD;
    private TextView tvTotalAmount;
    private Button btnCheckout;
    private double totalAmount;
    private List<CartItem> cartItems;
    private DatabaseReference ordersRef, orderDetailsRef, cartRef, userRef;
    private FirebaseUser currentUser;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        initViews();
        initFirebase();
        loadCartItems();
        setupListeners();
    }

    private void initViews() {
        etAddress = findViewById(R.id.etAddress);
        etPhone = findViewById(R.id.etPhone);
        etName = findViewById(R.id.etName);
        rgPaymentMethod = findViewById(R.id.rgPaymentMethod);
        rbOnline = findViewById(R.id.rbOnline);
        rbCOD = findViewById(R.id.rbCOD);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        btnCheckout = findViewById(R.id.btnCheckout);
        progressDialog = new ProgressDialog(this);
        cartItems = new ArrayList<>();

        // Hiển thị số dư tài khoản
        double balance = MyApplication.getCurrentUserInfo().getAccountBalance();
        rbOnline.setText("Thanh toán bằng số dư (Số dư: " + balance + "đ)");
        etAddress.setText(MyApplication.getCurrentUserInfo().getAddress().toString());
        etPhone.setText(MyApplication.getCurrentUserInfo().getPhoneNumber());
        etName.setText(MyApplication.getCurrentUserInfo().getDisplayName());
    }

    private void initFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        ordersRef = database.getReference("orders");
        orderDetailsRef = database.getReference("orderDetails");
        cartRef = database.getReference("carts").child(currentUser.getUid()).child("items");
        userRef = database.getReference("users").child(currentUser.getUid());
    }

    private void loadCartItems() {
        progressDialog.setMessage("Đang tải thông tin...");
        progressDialog.show();

        cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartItems.clear();
                totalAmount = 0;

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    CartItem item = dataSnapshot.getValue(CartItem.class);
                    if (item != null) {
                        cartItems.add(item);
                        totalAmount += item.getPrice() * item.getQuantity();
                    }
                }

                tvTotalAmount.setText("Tổng tiền: " + totalAmount + "đ");
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                showDialog("Lỗi", "Không thể tải thông tin giỏ hàng: " + error.getMessage(),
                        CustomDialogFragment.DialogType.ERROR, "Đóng");
            }
        });
    }

    private void setupListeners() {
        btnCheckout.setOnClickListener(v -> processCheckout());
    }

    private void processCheckout() {
        String address = etAddress.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String name = etName.getText().toString().trim();

        if (address.isEmpty() || phone.isEmpty()) {
            showDialog("Lỗi", "Vui lòng nhập đầy đủ thông tin",
                    CustomDialogFragment.DialogType.ERROR, "Đóng");
            return;
        }

        if(!rbOnline.isChecked() && !rbCOD.isChecked()) {
            showDialog("Lỗi", "Vui lòng chọn phương thức thanh toán",
                    CustomDialogFragment.DialogType.ERROR, "Đóng");
            return;
        }

        boolean isOnlinePayment = rbOnline.isChecked();

        if (isOnlinePayment) {
            double balance = MyApplication.getCurrentUserInfo().getAccountBalance();
            if (balance < totalAmount) {
                showDialog("Lỗi", "Số dư không đủ để thanh toán",
                        CustomDialogFragment.DialogType.ERROR, "Đóng");
                return;
            }
        }

        progressDialog.setMessage("Đang xử lý đơn hàng...");
        progressDialog.show();

        // Tạo đơn hàng mới
        String orderId = ordersRef.push().getKey();
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(currentUser.getUid());
        order.setTotalAmount(totalAmount);
        order.setAddress(address);
        order.setPhoneNumber(phone);
        order.setName(name);
        order.setStatus("PENDING");
        order.setPaymentMethod(isOnlinePayment ? "ONLINE" : "COD");
        order.setPaymentStatus(isOnlinePayment ? "PAID" : "PENDING");
        order.setCreatedAt(System.currentTimeMillis());

        // Lưu đơn hàng và chi tiết
        saveOrderAndDetails(order, isOnlinePayment);
    }

    private void saveOrderAndDetails(Order order, boolean isOnlinePayment) {
        DatabaseReference booksRef = FirebaseDatabase.getInstance().getReference("books");

        ordersRef.child(order.getId()).setValue(order)
                .addOnSuccessListener(aVoid -> {
                    // Lưu chi tiết đơn hàng và cập nhật số lượng sách
                    for (CartItem item : cartItems) {
                        // Lưu chi tiết đơn hàng
                        OrderDetail detail = new OrderDetail();
                        detail.setOrderId(order.getId());
                        detail.setBookId(item.getBookId());
                        detail.setBookTitle(item.getTitle());
                        detail.setBookImage(item.getCoverImage());
                        detail.setQuantity(item.getQuantity());
                        detail.setPrice(item.getPrice());
                        detail.setTotalPrice(item.getPrice() * item.getQuantity());

                        orderDetailsRef.push().setValue(detail);

                        // Giảm số lượng sách trong kho và tăng soldCount
                        booksRef.child(item.getBookId()).runTransaction(new Transaction.Handler() {
                            @NonNull
                            @Override
                            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                                Book book = mutableData.getValue(Book.class);
                                if (book == null) {
                                    return Transaction.success(mutableData);
                                }

                                // Kiểm tra số lượng sách
                                if (book.getStock() < item.getQuantity()) {
                                    // Không đủ số lượng
                                    return Transaction.abort();
                                }

                                // Giảm số lượng sách
                                book.setStock(book.getStock() - item.getQuantity());

                                // Tăng số lượng sách đã bán
                                book.setSoldCount(book.getSoldCount() + item.getQuantity());

                                // Cập nhật dữ liệu
                                mutableData.setValue(book);
                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                                if (databaseError != null) {
                                    // Xử lý lỗi
                                    runOnUiThread(() -> {
                                        showDialog("Lỗi", "Cập nhật số lượng sách thất bại: " + databaseError.getMessage(),
                                                CustomDialogFragment.DialogType.ERROR, "Đóng");
                                    });
                                } else if (!committed) {
                                    // Giao dịch bị hủy (không đủ số lượng)
                                    runOnUiThread(() -> {
                                        showDialog("Lỗi", "Sách " + item.getTitle() + " không đủ số lượng",
                                                CustomDialogFragment.DialogType.ERROR, "Đóng");
                                    });
                                }
                            }
                        });
                    }

                    if (isOnlinePayment) {
                        // Trừ tiền trong tài khoản
                        double newBalance = MyApplication.getCurrentUserInfo().getAccountBalance() - totalAmount;
                        userRef.child("accountBalance").setValue(newBalance);
                    }

                    // Xóa giỏ hàng
                    cartRef.removeValue();

                    progressDialog.dismiss();
                    showDialog("Thành công", "Đặt hàng thành công",
                            CustomDialogFragment.DialogType.SUCCESS, "Đóng");
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    showDialog("Lỗi", "Không thể xử lý đơn hàng: " + e.getMessage(),
                            CustomDialogFragment.DialogType.ERROR, "Đóng");
                });
    }

    private void showDialog(String title, String message, CustomDialogFragment.DialogType type, String buttonText) {
        CustomDialogFragment dialog = CustomDialogFragment.newInstance(
                type,
                title,
                message,
                buttonText
        );
        dialog.show(getSupportFragmentManager(), CustomDialogFragment.TAG);
    }
}