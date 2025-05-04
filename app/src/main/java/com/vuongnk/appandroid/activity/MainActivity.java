package com.vuongnk.appandroid.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.nex3z.notificationbadge.NotificationBadge;

import com.vuongnk.appandroid.R;
import com.vuongnk.appandroid.application.MyApplication;
//import com.vuongnk.appandroid.activity.ContactActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import com.vuongnk.appandroid.model.Book;
import com.vuongnk.appandroid.model.Category;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vuongnk.appandroid.adapter.BookAdapter;
import com.vuongnk.appandroid.adapter.CategoryAdapter;
import com.vuongnk.appandroid.util.GridSpacingItemDecoration;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    // Xử lý giao diện trang chủ
    private static final String TAG = "MainActivity";
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1;
    private ViewFlipper viewFlipper;
    private NavigationView navigationView;
    private DrawerLayout mDrawerLayout;
    private Toolbar toolbar;
    private ProgressDialog progressDialog;

    private CircleImageView img_user;

    // Xử lý giao diện sản phẩm + bắt sự kiện
    private RecyclerView rcv_list_item, rcv_categories;
    private ImageView imgsearch;
    private FrameLayout framegiohang;
    private List<Book> bookList;
    private List<Category> categoryList;
    private DatabaseReference databaseReference, categoryRef;
    private ValueEventListener booksListener, categoriesListener, cartListener;

    private BookAdapter bookAdapter;
    private CategoryAdapter categoryAdapter;
    private String currentCategoryId = null;


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


// xử lý giao diện trang chủ (thanh navigation view)
        initUI();
        setupToolbar();
        setupNavigation();
        setupSlider();


// Xử lý giao diện sản phẩm
        initFirebase();
        loadData();


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
        img_user = headerView.findViewById(R.id.img_user);

        // Main views
        rcv_list_item = findViewById(R.id.rcv_list_item);
        rcv_categories = findViewById(R.id.rcv_categories);
        imgsearch = findViewById(R.id.imgsearch);
        framegiohang = findViewById(R.id.framegiohang);


        // Initialize lists and dialog
        progressDialog = new ProgressDialog(this);
        bookList = new ArrayList<>();
        categoryList = new ArrayList<>();


    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

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


    // Xử lý giao diện Recycle View Sản phẩm ---------------
    private void initFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("books");
        categoryRef = database.getReference("categories");

    }

    private void loadData() {
        getCategories();
        getBooks();

    }

    private void getCategories() {
        progressDialog.setMessage("Đang tải danh mục...");
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }

        categoriesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                categoryList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Category category = snapshot.getValue(Category.class);
                    if (category != null) {
                        category.setId(snapshot.getKey());
                        categoryList.add(category);
                    }
                }
                setupCategoryRecyclerView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this,
                        "Lỗi khi tải danh mục: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        };

        categoryRef.addValueEventListener(categoriesListener);
    }

    private void setupCategoryRecyclerView() {
        categoryAdapter = new CategoryAdapter(this, categoryList, (category, isReselected) -> {
            if (isReselected) {
                // If the same category is clicked again, reset to show all books
                currentCategoryId = null;
                displayBooks(bookList);
            } else {
                // Filter books by the selected category
                currentCategoryId = category.getId();
               filterBooksByCategory(category.getId());
            }
        });

        rcv_categories.setAdapter(categoryAdapter);
        rcv_categories.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    private void getBooks() {
        if (!progressDialog.isShowing()) {
            progressDialog.setMessage("Đang tải dữ liệu...");
            progressDialog.show();
        }

        booksListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                bookList.clear();
                Log.d(TAG, "Số lượng sách từ Firebase: " + dataSnapshot.getChildrenCount());

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Book book = snapshot.getValue(Book.class);
                    if (book != null && book.isActive() == 0) {
                        // Ensure book has an ID
                        book.setId(snapshot.getKey());
                        Log.d(TAG, "Đọc sách: " + book.getTitle());
                        bookList.add(book);
                    }
                }

                Log.d(TAG, "Số lượng sách sau khi xử lý: " + bookList.size());
                if (bookList.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Không có sách nào", Toast.LENGTH_SHORT).show();
                }

// hiển thị sản phẩm ở Recycle View -------
                displayBooks(bookList);


                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Lỗi khi đọc dữ liệu: " + error.getMessage());
                Toast.makeText(MainActivity.this,
                        "Lỗi khi tải dữ liệu: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        };

        databaseReference.addValueEventListener(booksListener);
    }

    private void displayBooks(List<Book> books) {
        // Xóa tất cả item decoration cũ
        while (rcv_list_item.getItemDecorationCount() > 0) {
            rcv_list_item.removeItemDecorationAt(0);
        }

        // Tạo adapter mới
        bookAdapter = new BookAdapter(this, books);

        // Thiết lập layout manager
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        rcv_list_item.setLayoutManager(layoutManager);

        // Thêm item decoration mới
        int spacing = getResources().getDimensionPixelSize(R.dimen.spacing);
        rcv_list_item.addItemDecoration(new GridSpacingItemDecoration(2, spacing, true));

        // Thiết lập adapter và animation
        rcv_list_item.setAdapter(bookAdapter);
        rcv_list_item.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(
                this, R.anim.layout_animation_fall_down));
    }


    private void filterBooksByCategory(String categoryId) {
        if (bookList == null || bookList.isEmpty()) {
            Toast.makeText(this, "Không có sách để lọc", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Book> filteredBooks = new ArrayList<>();
        for (Book book : bookList) {
            Map<String, Boolean> categories = book.getCategories();
            if (categories != null && categories.containsKey(categoryId) && Boolean.TRUE.equals(categories.get(categoryId))) {
                filteredBooks.add(book);
            }
        }

        if (filteredBooks.isEmpty()) {
            Toast.makeText(this, "Không có sách trong danh mục này", Toast.LENGTH_SHORT).show();
        }

        displayBooks(filteredBooks);
    }
}