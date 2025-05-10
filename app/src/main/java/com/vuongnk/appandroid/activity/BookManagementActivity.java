package com.vuongnk.appandroid.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import com.vuongnk.appandroid.R;

import com.vuongnk.appandroid.model.Book;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.atomic.AtomicBoolean;

public class BookManagementActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private LinearLayout bookContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_management);

        toolbar = findViewById(R.id.toolbar);
        bookContainer = findViewById(R.id.book_container);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quản lý sách");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> finish());

        loadBooks();
    }

    private void loadBooks() {
        DatabaseReference booksRef = FirebaseDatabase.getInstance().getReference("books");
        booksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bookContainer.removeAllViews();
                if (!snapshot.exists()) {
                    TextView tvEmpty = new TextView(BookManagementActivity.this);
                    tvEmpty.setText("Chưa có sách nào");
                    tvEmpty.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    tvEmpty.setPadding(0, 32, 0, 32);
                    bookContainer.addView(tvEmpty);
                    return;
                }
                for (DataSnapshot bookSnap : snapshot.getChildren()) {
                    Book book = bookSnap.getValue(Book.class);
                    if (book != null) {
                        BookView(book);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BookManagementActivity.this,
                        "Lỗi tải sách: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void BookView(Book book) {
        View bookView = getLayoutInflater().inflate(R.layout.item_book_management, null);

        TextView tvTitle = bookView.findViewById(R.id.tv_book_title);
        TextView tvAuthor = bookView.findViewById(R.id.tv_book_author);
        TextView tvPrice = bookView.findViewById(R.id.tv_book_price);
        TextView tvStock = bookView.findViewById(R.id.tv_book_stock);
        ImageView imgBook = bookView.findViewById(R.id.img_book);
        Button btnDelete = bookView.findViewById(R.id.btn_delete);

        tvTitle.setText(book.getTitle());
        tvAuthor.setText(book.getAuthor());
        tvPrice.setText(String.format("%,.0f đ", book.getPrice()));
        tvStock.setText("Còn lại: " + book.getStock());

        Glide.with(this)
                .load(book.getCoverImage())
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .into(imgBook);

        btnDelete.setOnClickListener(v -> showDeleteConfirmDialog(book));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 16);
        bookView.setLayoutParams(params);
        bookContainer.addView(bookView);
    }

    private void showDeleteConfirmDialog(Book book) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa sách này?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteBook(book))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteBook(Book book) {
        DatabaseReference bookRef = FirebaseDatabase.getInstance()
                .getReference("books")
                .child(book.getId());

        bookRef.removeValue()
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Đã xóa sách", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Lỗi khi xóa sách: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }
}
