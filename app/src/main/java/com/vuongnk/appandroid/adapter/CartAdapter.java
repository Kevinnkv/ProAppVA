package com.vuongnk.appandroid.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.vuongnk.appandroid.R;
import com.vuongnk.appandroid.dialog.CustomDialogFragment;
import com.vuongnk.appandroid.model.CartItem;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DecimalFormat;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private Context context;
    private List<CartItem> cartItems;
    private OnCartActionListener listener;
    private DatabaseReference booksRef;

    public interface OnCartActionListener {
        void onQuantityChanged(CartItem item, int newQuantity, double newTotalprice);
        void onRemoveItem(CartItem item);
    }

    public CartAdapter(Context context, List<CartItem> cartItems, OnCartActionListener listener) {
        this.context = context;
        this.cartItems = cartItems;
        this.listener = listener;
        this.booksRef = FirebaseDatabase.getInstance().getReference("books");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        // Hiển thị ảnh bìa sách
        if (item.getCoverImage() != null && !item.getCoverImage().isEmpty()) {
            Glide.with(context)
                    .load(item.getCoverImage())
                    .placeholder(R.drawable.placeholder_book)
                    .error(R.drawable.placeholder_book)
                    .into(holder.imgBookCover);
        }

        // Hiển thị tiêu đề sách
        holder.tvBookTitle.setText(item.getTitle());

        // Format và hiển thị giá
        DecimalFormat formatter = new DecimalFormat("#,###");
        holder.tvBookPrice.setText(formatter.format(item.getPrice()) + " đ");

        // Hiển thị số lượng
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

        // Xử lý sự kiện giảm số lượng
        holder.btnDecrease.setOnClickListener(v -> {
            int newQuantity = item.getQuantity() - 1;
            double newTotalprice = newQuantity * item.getPrice();
            if (newQuantity >= 1 && listener != null) {
                listener.onQuantityChanged(item, newQuantity, newTotalprice);
            }
        });

        // Xử lý sự kiện tăng số lượng
        holder.btnIncrease.setOnClickListener(v -> {
            // Kiểm tra lại số lượng tồn mới nhất trước khi tăng
            booksRef.child(item.getBookId()).get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    int currentStock = snapshot.child("stock").getValue(Integer.class);
                    int newQuantity = item.getQuantity() + 1;

                    if (newQuantity > currentStock) {
                        showWarningDialog(context,
                                "Không đủ hàng",
                                "Số lượng trong kho chỉ còn " + currentStock + " sản phẩm",
                                "Đã hiểu");
                        return;
                    }

                    double newTotalPrice = newQuantity * item.getPrice();
                    if (listener != null) {
                        listener.onQuantityChanged(item, newQuantity, newTotalPrice);
                    }
                }
            });
        });

        // Xử lý sự kiện xóa sản phẩm
        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveItem(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems != null ? cartItems.size() : 0;
    }

    public void updateCartItems(List<CartItem> newItems) {
        this.cartItems = newItems;
        notifyDataSetChanged();
    }

    private void showWarningDialog(Context context, String title, String message, String buttonText) {
        CustomDialogFragment dialog = CustomDialogFragment.newInstance(
                CustomDialogFragment.DialogType.WARNING,
                title,
                message,
                buttonText
        );
        if (context instanceof AppCompatActivity) {
            dialog.show(((AppCompatActivity) context).getSupportFragmentManager(),
                    CustomDialogFragment.TAG);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgBookCover;
        TextView tvBookTitle, tvBookPrice, tvQuantity;
        ImageButton btnDecrease, btnIncrease, btnRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgBookCover = itemView.findViewById(R.id.img_book_cover);
            tvBookTitle = itemView.findViewById(R.id.tv_book_title);
            tvBookPrice = itemView.findViewById(R.id.tv_book_price);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            btnDecrease = itemView.findViewById(R.id.btn_decrease);
            btnIncrease = itemView.findViewById(R.id.btn_increase);
            btnRemove = itemView.findViewById(R.id.btn_remove);
        }
    }
}
