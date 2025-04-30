package com.vuongnk.appandroid.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;


import com.nex3z.notificationbadge.NotificationBadge;


import com.vuongnk.appandroid.R;
import com.vuongnk.appandroid.application.MyApplication;
import com.vuongnk.appandroid.activity.ContactActivity;

import java.util.ArrayList;
import java.util.List;


import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static final String TAG = "MainActivity";
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1;
    private ViewFlipper viewFlipper;
    private NavigationView navigationView;
    private DrawerLayout mDrawerLayout;
    private Toolbar toolbar;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private TextView tvName, tvEmail, tv_balance;
    private CircleImageView img_user;
    //private String currentCategoryId = null;
    private ValueEventListener booksListener, categoriesListener, cartListener;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Kiểm tra login
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initUI();
        setupToolbar();
        setupNavigation();
        setupSlider();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION);
            }
        }
    }
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
        viewFlipper = findViewById(R.id.viewlipper);
        navigationView = findViewById(R.id.navigation_view);
        mDrawerLayout = findViewById(R.id.main);

        // Navigation header views
        View headerView = navigationView.getHeaderView(0);
        tvName = headerView.findViewById(R.id.tv_name);
        tvEmail = headerView.findViewById(R.id.tv_email);
        tv_balance = headerView.findViewById(R.id.tv_balance);
        img_user = headerView.findViewById(R.id.img_user);

        // Main views
//        rcv_list_item = findViewById(R.id.rcv_list_item);
//       // rcv_categories = findViewById(R.id.rcv_categories);
//        imgsearch = findViewById(R.id.imgsearch);
//        framegiohang = findViewById(R.id.framegiohang);
       // badge = findViewById(R.id.menu_sl);

        // Initialize lists and dialog
        progressDialog = new ProgressDialog(this);
//        bookList = new ArrayList<>();
//        categoryList = new ArrayList<>();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        DrawerLayout mDrawerLayout = findViewById(R.id.main);
        // cuc sua phan nay
        if (id == R.id.nav_home) {
            // Nếu đã ở MainActivity thì không làm gì cả
        } else if (id == R.id.nav_logo_out) {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Đang đăng xuất...");
            progressDialog.show();

            MyApplication.handleLogout();

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_contact) {
            startActivity(new Intent(getApplicationContext(), ContactActivity.class));
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // su dung khi dung SharePreference
//    private void handleLogout() {
//        progressDialog.setMessage("Đang đăng xuất...");
//        progressDialog.show();
//
//        MyApplication.handleLogout();
//
//        progressDialog.dismiss();
//
//        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(intent);
//        finish();
//    }

    private void setupNavigation() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void setupSlider() {
        List<Integer> bannerImages = new ArrayList<>();
        bannerImages.add(R.drawable.img_slide_1);
        bannerImages.add(R.drawable.img_slide_2);
        bannerImages.add(R.drawable.img_slide_3);

        for (Integer imageResource : bannerImages) {
            ImageView imageView = new ImageView(getApplicationContext());
            imageView.setImageResource(imageResource);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            viewFlipper.addView(imageView);
        }

        // Set animation and auto-flip
        viewFlipper.setFlipInterval(3000);
        viewFlipper.setAutoStart(true);
        Animation slideIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_right);
        Animation slideOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_rigth);
        viewFlipper.setInAnimation(slideIn);
        viewFlipper.setOutAnimation(slideOut);
    }

}