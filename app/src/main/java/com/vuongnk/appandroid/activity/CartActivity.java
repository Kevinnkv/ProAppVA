package com.vuongnk.appandroid.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vuongnk.appandroid.R;
import com.vuongnk.appandroid.adapter.CartAdapter;
import com.vuongnk.appandroid.model.CartItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartActionListener {
    private RecyclerView rcvCartItems;
    private TextView tvTotalAmount;
    private Button btnCheckout;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems;
    private DatabaseReference cartRef;
    private FirebaseUser currentUser;
    private double totalAmount = 0;
    private View layoutEmpty;
    private View layoutContent;
    private Button btnContinueShopping;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }

        initUI();
        setupToolbar();
        setupRecyclerView();
        loadCartItems();
    }

    private void initUI() {
        rcvCartItems = findViewById(R.id.rcv_cart_items);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        btnCheckout = findViewById(R.id.btn_checkout);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        layoutContent = findViewById(R.id.layoutContent);
        btnContinueShopping = findViewById(R.id.btnContinueShopping);

        btnCheckout.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(new Intent(this, CheckoutActivity.class));
        });

        btnContinueShopping.setOnClickListener(v -> {
            finish(); // Quay lại màn hình trước đó
        });
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

    private void setupRecyclerView() {
        cartItems = new ArrayList<>();
        cartAdapter = new CartAdapter(this, cartItems, this);
        rcvCartItems.setLayoutManager(new LinearLayoutManager(this));
        rcvCartItems.setAdapter(cartAdapter);
        cartRef = FirebaseDatabase.getInstance().getReference("carts")
                .child(currentUser.getUid()).child("items");
    }

    private void loadCartItems() {
        cartRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                cartItems.clear();
                totalAmount = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    CartItem item = snapshot.getValue(CartItem.class);
                    if (item != null) {
                        cartItems.add(item);
                        totalAmount += item.getTotalPrice();
                    }
                }

                cartAdapter.updateCartItems(cartItems);
                updateTotalAmount();
                updateEmptyState();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CartActivity.this,
                        "Lỗi: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTotalAmount() {
        DecimalFormat formatter = new DecimalFormat("#,###");
        tvTotalAmount.setText(formatter.format(totalAmount) + " đ");
    }

    private void updateEmptyState() {
        if (cartItems.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            layoutContent.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            layoutContent.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onQuantityChanged(CartItem item, int newQuantity, double newTotalPrice) {
        String itemId = item.getBookId();
        Map<String, Object> updates = new HashMap<>();
        updates.put("quantity", newQuantity);
        updates.put("totalPrice", newTotalPrice);

        cartRef.child(itemId).updateChildren(updates)
                .addOnFailureListener(e -> Toast.makeText(this,
                        "Lỗi khi cập nhật giỏ hàng",
                        Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onRemoveItem(CartItem item) {
        String itemId = item.getBookId();
        cartRef.child(itemId).removeValue()
                .addOnSuccessListener(aVoid -> Toast.makeText(this,
                        "Đã xóa sản phẩm khỏi giỏ hàng",
                        Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this,
                        "Lỗi khi xóa sản phẩm",
                        Toast.LENGTH_SHORT).show());
    }
}