package com.vuongnk.appandroid.activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vuongnk.appandroid.R;
import com.vuongnk.appandroid.adapter.CommentAdapter;
import com.vuongnk.appandroid.application.MyApplication;
import com.vuongnk.appandroid.dialog.CustomDialogFragment;
import com.vuongnk.appandroid.model.Book;
import com.vuongnk.appandroid.model.CartItem;
import com.vuongnk.appandroid.model.Comment;
import com.vuongnk.appandroid.model.Rating;
import com.vuongnk.appandroid.model.User;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nex3z.notificationbadge.NotificationBadge;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookDetailActivity extends AppCompatActivity  {
    private ImageView imgBookCover;
    private TextView tvBookTitle, tvBookAuthor, tvBookPrice, tvBookOriginalPrice,
            tvBookStock, tvBookDescription;
    private RatingBar ratingBar, ratingBarUser;
    private TextView tvRatingCount;
    private EditText etComment, etRatingContent;
    private ImageButton btnSendComment;
    private Button btnAddToCart, btnSubmitRating;
    private RecyclerView rcvComments;
    private Book book;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;
    private DatabaseReference commentsRef, ratingsRef;
    private FirebaseUser currentUser;
    private FrameLayout framegiohang;
    private NotificationBadge badge;
    private DatabaseReference cartRef;
    private User userInfo = MyApplication.getCurrentUserInfo();
    private LinearLayout layoutUserRating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        // Nhận dữ liệu sách từ intent
        book = (Book) getIntent().getSerializableExtra("book");
        if (book == null) {
            finish();
            return;
        }

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }

        initUI();
        setupToolbar();
        setupComments();
        displayBookDetails();
        loadUserRating();
        setupCartBadge();
        loadComments();
        setupListeners();
        setupRating();
    }

    private void initUI() {
        imgBookCover = findViewById(R.id.img_book_cover);
        tvBookTitle = findViewById(R.id.tv_book_title);
        tvBookAuthor = findViewById(R.id.tv_book_author);
        tvBookPrice = findViewById(R.id.tv_book_price);
        tvBookOriginalPrice = findViewById(R.id.tv_book_original_price);
        tvBookStock = findViewById(R.id.tv_book_stock);
        tvBookDescription = findViewById(R.id.tv_book_description);
        ratingBar = findViewById(R.id.rating_bar);
        ratingBarUser = findViewById(R.id.rating_bar_user);
        btnAddToCart = findViewById(R.id.btn_add_to_cart);
        btnSubmitRating = findViewById(R.id.btn_submit_rating);
        etComment = findViewById(R.id.et_comment);
        etRatingContent = findViewById(R.id.et_rating_content);
        btnSendComment = findViewById(R.id.btn_send_comment);
        rcvComments = findViewById(R.id.rcv_comments);
        framegiohang = findViewById(R.id.framegiohang);
        badge = findViewById(R.id.menu_sl);
        layoutUserRating = findViewById(R.id.layout_user_rating);
        tvRatingCount = findViewById(R.id.tv_rating_count);

        ratingsRef = FirebaseDatabase.getInstance().getReference("ratings");
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

    private void setupComments() {
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, currentUser.getUid());
        rcvComments.setLayoutManager(new LinearLayoutManager(this));
        rcvComments.setAdapter(commentAdapter);
        commentsRef = FirebaseDatabase.getInstance().getReference("comments");
    }

    private void displayBookDetails() {
        // Hiển thị ảnh bìa sách
        if (book.getCoverImage() != null && !book.getCoverImage().isEmpty()) {
            Glide.with(this)
                    .load(book.getCoverImage())
                    .placeholder(R.drawable.placeholder_book)
                    .error(R.drawable.placeholder_book)
                    .into(imgBookCover);
        }

        // Hiển thị thông tin sách
        tvBookTitle.setText(book.getTitle());
        tvBookAuthor.setText(book.getAuthor());
        tvBookDescription.setText(book.getDescription());

        // Format và hiển thị giá
        DecimalFormat formatter = new DecimalFormat("#,###");
        double originalPrice = book.getPrice();
        double discountedPrice = book.getFinalPrice();

        tvBookPrice.setText(formatter.format(discountedPrice) + " đ");
        if (book.getDiscount() > 0) {
            tvBookOriginalPrice.setText(formatter.format(originalPrice) + " đ");
            tvBookOriginalPrice.setVisibility(View.VISIBLE);
        } else {
            tvBookOriginalPrice.setVisibility(View.GONE);
        }

        // Hiển thị số lượng còn lại
        tvBookStock.setText("Còn lại: " + book.getStock() + " cuốn");

        // Hiển thị đánh giá
        ratingBar.setRating((float) book.getAverageRating());
        tvRatingCount.setText(String.format("(%d đánh giá)", book.getRatingCount()));
    }

    private void setupListeners() {
        btnAddToCart.setOnClickListener(v -> addToCart());
        btnSendComment.setOnClickListener(v -> sendComment());
        framegiohang.setOnClickListener(v -> {
            Intent intent = new Intent(BookDetailActivity.this, CartActivity.class);
            startActivity(intent);
        });
    }

    private void addToCart() {
        if (currentUser == null || book == null) {
            return;
        }

        // Kiểm tra số lượng tồn
        if (book.getStock() <= 0) {
            showWarningDialog("Hết hàng",
                    "Sản phẩm này hiện đã hết hàng. Vui lòng quay lại sau.",
                    "Đóng");
            return;
        }

        DatabaseReference cartRef = FirebaseDatabase.getInstance()
                .getReference("carts")
                .child(currentUser.getUid())
                .child("items")
                .child(book.getId());

        cartRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    CartItem existingItem = snapshot.getValue(CartItem.class);
                    if (existingItem != null) {
                        int newQuantity = existingItem.getQuantity() + 1;

                        // Kiểm tra nếu số lượng mới vượt quá số lượng tồn
                        if (newQuantity > book.getStock()) {
                            showWarningDialog("Không đủ hàng",
                                    "Số lượng trong kho chỉ còn " + book.getStock() + " sản phẩm",
                                    "Đã hiểu");
                            return;
                        }

                        existingItem.setQuantity(newQuantity);
                        cartRef.setValue(existingItem)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Đã cập nhật số lượng trong giỏ hàng", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this,
                                            "Lỗi khi cập nhật giỏ hàng: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });
                    }
                } else {
                    // Nếu sản phẩm chưa có trong giỏ hàng, thêm mới với số lượng 1
                    // (Đã kiểm tra số lượng tồn > 0 ở trên)
                    CartItem cartItem = new CartItem(
                            book.getId(),
                            book.getFinalPrice(),
                            1,
                            book.getTitle(),
                            book.getCoverImage()
                    );

                    cartRef.setValue(cartItem)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this,
                                        "Lỗi khi thêm vào giỏ hàng: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                }
            } else {
                Toast.makeText(this,
                        "Lỗi khi kiểm tra giỏ hàng: " + task.getException().getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showWarningDialog(String title, String message, String buttonText) {
        CustomDialogFragment dialog = CustomDialogFragment.newInstance(
                CustomDialogFragment.DialogType.WARNING,
                title,
                message,
                buttonText
        );
        dialog.show(getSupportFragmentManager(), CustomDialogFragment.TAG);
    }

    private void loadComments() {
        commentsRef.orderByChild("bookId").equalTo(book.getId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<Comment> allComments = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Comment comment = snapshot.getValue(Comment.class);
                            if (comment != null) {
                                comment.setId(snapshot.getKey());
                                allComments.add(comment);
                            }
                        }
                        commentAdapter.setComments(allComments);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(BookDetailActivity.this,
                                "Lỗi khi tải bình luận: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendComment() {
        String content = etComment.getText().toString().trim();

        if (TextUtils.isEmpty(content)) {
            etComment.setError("Vui lòng nhập nội dung bình luận");
            return;
        }

        String uid = currentUser.getUid();
        String displayName = userInfo != null ? userInfo.getDisplayName() : currentUser.getDisplayName();
        String photoUrl = userInfo != null ? userInfo.getPhotoURL() :
                (currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : "");

        // Tạo comment mới với Map likes rỗng
        Comment comment = new Comment();
        comment.setBookId(book.getId());
        comment.setContent(content);
        comment.setUserId(uid);
        comment.setUserDisplayName(displayName);
        comment.setUserPhotoUrl(photoUrl);
        comment.setCreatedAt(System.currentTimeMillis());
        comment.setParentId(""); // Comment gốc
        comment.setLikes(new HashMap<>()); // Khởi tạo Map likes rỗng

        // Lưu comment vào database
        String commentId = commentsRef.push().getKey();
        if (commentId != null) {
            comment.setId(commentId);
            commentsRef.child(commentId).setValue(comment)
                    .addOnSuccessListener(aVoid -> {
                        etComment.setText("");
                        Toast.makeText(BookDetailActivity.this,
                                "Đã đăng bình luận",
                                Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(BookDetailActivity.this,
                            "Lỗi khi đăng bình luận: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show());
        }
    }

    private void setupCartBadge() {
        cartRef = FirebaseDatabase.getInstance().getReference("carts")
                .child(currentUser.getUid())
                .child("items");

        cartRef.addValueEventListener(new ValueEventListener() {
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
                Toast.makeText(BookDetailActivity.this,
                        "Lỗi khi tải giỏ hàng: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRating() {
        tvRatingCount.setText(String.format("(%d đánh giá)", book.getRatingCount()));

        btnSubmitRating.setOnClickListener(v -> submitRating());
    }

    private void loadUserRating() {
        if (currentUser == null) return;

        ratingsRef.orderByChild("userId").equalTo(currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Rating rating = snapshot.getValue(Rating.class);
                            if (rating != null && rating.getBookId().equals(book.getId())) {
                                // Hiển thị đánh giá cũ của user
                                ratingBarUser.setRating(rating.getRating());
                                etRatingContent.setText(rating.getContent());
                                btnSubmitRating.setText("Cập nhật đánh giá");
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(BookDetailActivity.this,
                                "Lỗi khi tải đánh giá: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void submitRating() {
        float rating = ratingBarUser.getRating();
        if (rating == 0) {
            Toast.makeText(this, "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo đối tượng Rating mới
        Rating newRating = new Rating(
                book.getId(),
                etRatingContent.getText().toString().trim(),
                rating,
                currentUser.getUid(),
                System.currentTimeMillis()
        );

        // Kiểm tra xem user đã đánh giá chưa
        ratingsRef.orderByChild("userId").equalTo(currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String ratingId = null;
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Rating oldRating = snapshot.getValue(Rating.class);
                            if (oldRating != null && oldRating.getBookId().equals(book.getId())) {
                                ratingId = snapshot.getKey();
                                break;
                            }
                        }

                        if (ratingId == null) {
                            // Thêm đánh giá mới
                            ratingId = ratingsRef.push().getKey();
                        }

                        if (ratingId != null) {
                            ratingsRef.child(ratingId).setValue(newRating)
                                    .addOnSuccessListener(aVoid -> {
                                        updateBookRating();
                                        Toast.makeText(BookDetailActivity.this,
                                                "Đã gửi đánh giá",
                                                Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(BookDetailActivity.this,
                                            "Lỗi khi gửi đánh giá: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(BookDetailActivity.this,
                                "Lỗi: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateBookRating() {
        DatabaseReference bookRef = FirebaseDatabase.getInstance()
                .getReference("books")
                .child(book.getId());

        ratingsRef.orderByChild("bookId").equalTo(book.getId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        double totalRating = 0;
                        int count = 0;

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Rating rating = snapshot.getValue(Rating.class);
                            if (rating != null) {
                                totalRating += rating.getRating();
                                count++;
                            }
                        }

                        double averageRating = count > 0 ? totalRating / count : 0;

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("averageRating", averageRating);
                        updates.put("ratingCount", count);

                        bookRef.updateChildren(updates);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(BookDetailActivity.this,
                                "Lỗi khi cập nhật rating: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}