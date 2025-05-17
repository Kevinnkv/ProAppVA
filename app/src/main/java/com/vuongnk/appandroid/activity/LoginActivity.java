package com.vuongnk.appandroid.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.vuongnk.appandroid.R;
import com.vuongnk.appandroid.adapter.LoginPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.widget.Toast;

import android.content.SharedPreferences;
import android.content.Context;
public class LoginActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private LoginPagerAdapter pagerAdapter;

    private FirebaseAuth mAuth; // đối tượng xác thực firebase
    private FirebaseAuth.AuthStateListener authListener; // lắng nghe trạng thái đăng nhập firebase

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Cấu hình để tránh bàn phím đẩy layout
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        // Ánh xạ view
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);

        // Thiết lập ViewPager với adapter
        pagerAdapter = new LoginPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Liên kết TabLayout với ViewPager2
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText("Đăng nhập");
                    } else {
                        tab.setText("Đăng ký");
                    }
                }).attach();

        // Ẩn bàn phím khi chuyển tab
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                hideKeyboard();
            }
        });

        // cuc thêm phần khởi tạo cho authListener để vào đăng nhập ở ngay trong hàm này
        // Khởi tạo FirebaseAuth và AuthStateListener
        mAuth = FirebaseAuth.getInstance();
        authListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                String role = sp.getString("role", "user");
                Class<?> target = "admin".equals(role)
                        ? AdminMainActivity.class
                        : MainActivity.class;
                Intent it = new Intent(LoginActivity.this, target);
                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(it);
                finish();
            }
        };
    }

    private void hideKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authListener != null) {
            mAuth.removeAuthStateListener(authListener);
        }
    }
}
