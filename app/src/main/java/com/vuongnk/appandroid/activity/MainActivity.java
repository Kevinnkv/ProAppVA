package com.vuongnk.appandroid.activity;

<<<<<<< HEAD
=======

import static android.os.Build.*;
import androidx.core.app.ActivityCompat;
>>>>>>> fdfcc2b (Initial commit)
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
<<<<<<< HEAD
import android.os.Build;
import android.os.Bundle;
=======
import android.os.Bundle;
import android.util.Log;
>>>>>>> fdfcc2b (Initial commit)
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
<<<<<<< HEAD
import android.widget.FrameLayout;
import android.widget.ImageView;
=======
import android.widget.ImageView;
import android.widget.FrameLayout;
>>>>>>> fdfcc2b (Initial commit)
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

<<<<<<< HEAD
import androidx.activity.EdgeToEdge;
=======
>>>>>>> fdfcc2b (Initial commit)
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
<<<<<<< HEAD
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

=======
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vuongnk.appandroid.R;
import com.vuongnk.appandroid.adapter.BookAdapter;
import com.vuongnk.appandroid.adapter.CategoryAdapter;
import com.vuongnk.appandroid.application.MyApplication;
import com.vuongnk.appandroid.model.Book;
import com.vuongnk.appandroid.model.CartItem;
import com.vuongnk.appandroid.model.Category;
import com.vuongnk.appandroid.util.GridSpacingItemDecoration;
import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nex3z.notificationbadge.NotificationBadge;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
>>>>>>> fdfcc2b (Initial commit)
    private static final String TAG = "MainActivity";
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1;
    private ViewFlipper viewFlipper;
    private NavigationView navigationView;
    private DrawerLayout mDrawerLayout;
    private Toolbar toolbar;
    private ProgressDialog progressDialog;
<<<<<<< HEAD
    private FirebaseAuth mAuth;
    private TextView tvName, tvEmail, tv_balance;
    private CircleImageView img_user;
    //private String currentCategoryId = null;
    private ValueEventListener booksListener, categoriesListener, cartListener;

=======
    private RecyclerView rcv_list_item, rcv_categories;
    private BookAdapter bookAdapter;
    private CategoryAdapter categoryAdapter;
    private ImageView imgsearch;
    private FrameLayout framegiohang;
    private List<Book> bookList;
    private List<Category> categoryList;
    private DatabaseReference databaseReference, categoryRef;
    private FirebaseAuth mAuth;
    private NotificationBadge badge;
    private DatabaseReference cartRef;
    private FirebaseUser currentUser;
    private TextView tvName, tvEmail, tv_balance;
    private CircleImageView img_user;
    private String currentCategoryId = null;
    private ValueEventListener booksListener, categoriesListener, cartListener;

    @Override
>>>>>>> fdfcc2b (Initial commit)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

<<<<<<< HEAD
        // Kiểm tra login
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
=======
        if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION);
            }
        }

        // Check if user is logged in
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
>>>>>>> fdfcc2b (Initial commit)
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

<<<<<<< HEAD
=======
        mAuth = FirebaseAuth.getInstance();
>>>>>>> fdfcc2b (Initial commit)
        initUI();
        setupToolbar();
        setupNavigation();
        setupSlider();
<<<<<<< HEAD

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION);
            }
        }
    }
=======
        initFirebase();
        loadData();
        initListeners();
        showUserInfo();
    }

    @Override
>>>>>>> fdfcc2b (Initial commit)
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
<<<<<<< HEAD
//        rcv_list_item = findViewById(R.id.rcv_list_item);
//       // rcv_categories = findViewById(R.id.rcv_categories);
//        imgsearch = findViewById(R.id.imgsearch);
//        framegiohang = findViewById(R.id.framegiohang);
       // badge = findViewById(R.id.menu_sl);

        // Initialize lists and dialog
        progressDialog = new ProgressDialog(this);
//        bookList = new ArrayList<>();
//        categoryList = new ArrayList<>();
=======
        rcv_list_item = findViewById(R.id.rcv_list_item);
        rcv_categories = findViewById(R.id.rcv_categories);
        imgsearch = findViewById(R.id.imgsearch);
        framegiohang = findViewById(R.id.framegiohang);
        badge = findViewById(R.id.menu_sl);

        // Initialize lists and dialog
        progressDialog = new ProgressDialog(this);
        bookList = new ArrayList<>();
        categoryList = new ArrayList<>();
>>>>>>> fdfcc2b (Initial commit)
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

<<<<<<< HEAD
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
=======
    private void setupNavigation() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
>>>>>>> fdfcc2b (Initial commit)
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);
    }

<<<<<<< HEAD
    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

=======
>>>>>>> fdfcc2b (Initial commit)
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

<<<<<<< HEAD
=======
    private void initFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("books");
        categoryRef = database.getReference("categories");
        cartRef = database.getReference("carts").child(currentUser.getUid()).child("items");
    }

    private void loadData() {
        getCategories();
        getBooks();
        setupCartBadge();
    }

    private void initListeners() {

        framegiohang.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CartActivity.class));
        });
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


    private void showUserInfo() {
        if (MyApplication.getCurrentUserInfo() != null) {
            tvName.setText(MyApplication.getCurrentUserInfo().getDisplayName());
            tvEmail.setText(MyApplication.getCurrentUserInfo().getEmail());
            tv_balance.setText("Số dư: " + MyApplication.getCurrentUserInfo().getAccountBalance() + " VND");

            Glide.with(MainActivity.this)
                    .load(MyApplication.getCurrentUserInfo().getPhotoURL())
                    .error(R.drawable.baseline_person_24)
                    .into(img_user);
        }
    }

    private void setupCartBadge() {
        cartListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int totalItems = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    CartItem item = snapshot.getValue(CartItem.class);
                    if (item != null) {
                        totalItems += item.getQuantity();
                    }
                }

                if (totalItems > 0) {
                    badge.setVisibility(View.VISIBLE);
                    badge.setText(String.valueOf(totalItems));
                } else {
                    badge.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this,
                        "Lỗi khi tải giỏ hàng: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        };

        cartRef.addValueEventListener(cartListener);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Nếu đã ở MainActivity thì không làm gì cả
        } else if (id == R.id.nav_logo_out) {
            handleLogout();
        } else if (id == R.id.nav_order) {
            startActivity(new Intent(getApplicationContext(), ListOrderActivity.class));
        } else if (id == R.id.nav_contact) {
            startActivity(new Intent(getApplicationContext(), ContactActivity.class));
        }else if (id == R.id.nav_noti) {
            startActivity(new Intent(getApplicationContext(), NotificationActivity.class));
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void handleLogout() {
        progressDialog.setMessage("Đang đăng xuất...");
        progressDialog.show();

        MyApplication.handleLogout();

        progressDialog.dismiss();

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove listeners to prevent memory leaks
        if (booksListener != null) {
            databaseReference.removeEventListener(booksListener);
        }

        if (categoriesListener != null) {
            categoryRef.removeEventListener(categoriesListener);
        }

        if (cartListener != null) {
            cartRef.removeEventListener(cartListener);
        }

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
>>>>>>> fdfcc2b (Initial commit)
}