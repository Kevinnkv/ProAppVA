package com.vuongnk.appandroid.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vuongnk.appandroid.R;
import com.vuongnk.appandroid.adapter.UserAdapter;
import com.vuongnk.appandroid.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserManagementActivity extends AppCompatActivity implements UserAdapter.OnUserActionListener {
    private Toolbar toolbar;
    private RecyclerView recyclerUsers;
    private Button btnAddUser;
    private UserAdapter adapter;
    private List<User> userList;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        initUI();
        setupToolbar();
        setupRecyclerView();
        setupFirebase();
        loadUsers();
    }

    private void initUI() {
        toolbar = findViewById(R.id.toolbar);
        recyclerUsers = findViewById(R.id.recycler_users);
        btnAddUser = findViewById(R.id.btn_add_user);
        userList = new ArrayList<>();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quản lý người dùng");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new UserAdapter(userList, this);
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerUsers.setAdapter(adapter);
    }

    private void setupFirebase() {
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");
    }

    private void loadUsers() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    Log.d("userSnapshot", userSnapshot.toString());
                    User user = userSnapshot.getValue(User.class);
                    if (user != null) {
                        userList.add(user);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserManagementActivity.this,
                    "Lỗi: " + error.getMessage(),
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEditUser(User user) {
        showEditUserDialog(user);
    }

    @Override
    public void onDeleteUser(User user) {
        new AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc muốn xóa người dùng này?")
            .setPositiveButton("Xóa", (dialog, which) -> {
                usersRef.child(user.getUid()).removeValue()
                    .addOnSuccessListener(aVoid -> Toast.makeText(this,
                        "Đã xóa người dùng",
                        Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this,
                        "Lỗi: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    @Override
    public void onToggleUserStatus(User user) {
        String message = user.isAccountActive() ?
            "Bạn có chắc muốn khóa tài khoản này?" :
            "Bạn có chắc muốn mở khóa tài khoản này?";

        new AlertDialog.Builder(this)
            .setTitle("Xác nhận")
            .setMessage(message)
            .setPositiveButton("Đồng ý", (dialog, which) -> {
                // Đảo ngược trạng thái active
                int newStatus = user.isAccountActive() ? 0 : 1;

                usersRef.child(user.getUid()).child("isActive").setValue(newStatus)
                    .addOnSuccessListener(aVoid -> {
                        String successMessage = newStatus == 1 ?
                            "Đã mở khóa tài khoản" :
                            "Đã khóa tài khoản";
                        Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this,
                        "Lỗi: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void showEditUserDialog(User user) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_user, null);
        EditText etName = dialogView.findViewById(R.id.et_name);
        EditText etEmail = dialogView.findViewById(R.id.et_email);
        EditText etPhone = dialogView.findViewById(R.id.et_phone);
        EditText etAccountBalance = dialogView.findViewById(R.id.et_accountBalance);

        etName.setText(user.getDisplayName());
        etEmail.setText(user.getEmail());
        etPhone.setText(user.getPhoneNumber());
        etAccountBalance.setText(String.valueOf(user.getAccountBalance()));

        new AlertDialog.Builder(this)
            .setTitle("Chỉnh sửa người dùng")
            .setView(dialogView)
            .setPositiveButton("Lưu", (dialog, which) -> {
                Map<String, Object> updates = new HashMap<>();
                updates.put("displayName", etName.getText().toString());
                updates.put("phoneNumber", etPhone.getText().toString());
                updates.put("accountBalance", Long.valueOf(etAccountBalance.getText().toString()));

                usersRef.child(user.getUid()).updateChildren(updates)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this,
                        "Đã cập nhật thông tin",
                        Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this,
                        "Lỗi: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
}