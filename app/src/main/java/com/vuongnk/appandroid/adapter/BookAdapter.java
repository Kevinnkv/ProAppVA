package com.vuongnk.appandroid.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.vuongnk.appandroid.R;

import com.vuongnk.appandroid.activity.BookDetailActivity;
import com.vuongnk.appandroid.model.Book;
import com.bumptech.glide.Glide;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.List;

//import com.vuongnk.appandroid.activity.BookDetailActivity;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.ViewHolder> {

    private Context context;
    private List<Book> bookList;

    public BookAdapter(Context context, List<Book> bookList) {
        this.context = context;
        this.bookList = bookList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Book book = bookList.get(position);

        // Thiết lập dữ liệu cho mỗi item
        holder.tvTitle.setText(book.getTitle());
        holder.tvAuthor.setText(book.getAuthor());

        // Format giá tiền
        DecimalFormat formatter = new DecimalFormat("#,###");
        String formattedPrice = formatter.format(book.getPrice()) + " đ";
        holder.tvPrice.setText(formattedPrice);

        // Nếu có giảm giá
        if (book.getDiscount() > 0) {
            // Tính giá sau giảm giá
            String formattedDiscountPrice = formatter.format(book.getFinalPrice()) + " đ";
            holder.tvDiscountPrice.setText(formattedDiscountPrice);
            holder.tvDiscountPrice.setVisibility(View.VISIBLE);
            holder.tvDiscount.setText("-" + book.getDiscount() + "%");
            holder.tvDiscount.setVisibility(View.VISIBLE);
            holder.tvPrice.setTextColor(context.getResources().getColor(R.color.colorGray));
        } else {
            holder.tvDiscountPrice.setVisibility(View.GONE);
            holder.tvDiscount.setVisibility(View.GONE);
        }

        // Load ảnh bìa sách
        if (book.getCoverImage() != null && !book.getCoverImage().isEmpty()) {
            Glide.with(context)
                    .load(book.getCoverImage())
                    .placeholder(R.drawable.placeholder_book)
                    .error(R.drawable.placeholder_book)
                    .into(holder.imgCover);
        } else {
            holder.imgCover.setImageResource(R.drawable.placeholder_book);
        }

        // Xử lý sự kiện khi nhấn vào item -> Về sau add Giao diện thông tin sản phẩm
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, BookDetailActivity.class);
            intent.putExtra("book", book);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return bookList != null ? bookList.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCover;
        TextView tvTitle, tvAuthor, tvPrice, tvDiscountPrice, tvDiscount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCover = itemView.findViewById(R.id.img_book_cover);
            tvTitle = itemView.findViewById(R.id.tv_book_title);
            tvAuthor = itemView.findViewById(R.id.tv_book_author);
            tvPrice = itemView.findViewById(R.id.tv_book_price);
            tvDiscountPrice = itemView.findViewById(R.id.tv_book_discount_price);
            tvDiscount = itemView.findViewById(R.id.tv_discount_percent);
        }
    }


}