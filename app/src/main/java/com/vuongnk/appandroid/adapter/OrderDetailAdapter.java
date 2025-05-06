package com.vuongnk.appandroid.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vuongnk.appandroid.R;
import com.vuongnk.appandroid.model.OrderDetail;
import com.bumptech.glide.Glide;

import java.text.DecimalFormat;
import java.util.List;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.ViewHolder> {
    private Context context;
    private List<OrderDetail> orderDetails;

    public OrderDetailAdapter(Context context, List<OrderDetail> orderDetails) {
        this.context = context;
        this.orderDetails = orderDetails;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderDetail detail = orderDetails.get(position);
        DecimalFormat formatter = new DecimalFormat("#,###");

        // Hiển thị ảnh sản phẩm
        if (detail.getBookImage() != null && !detail.getBookImage().isEmpty()) {
            Glide.with(context)
                    .load(detail.getBookImage())
                    .placeholder(R.drawable.placeholder_book)
                    .error(R.drawable.placeholder_book)
                    .into(holder.imgBook);
        }

        // Hiển thị thông tin sản phẩm
        holder.tvBookTitle.setText(detail.getBookTitle());
        holder.tvQuantity.setText("Số lượng: " + detail.getQuantity());
        holder.tvPrice.setText(formatter.format(detail.getPrice()) + "đ");
        holder.tvTotalPrice.setText(formatter.format(detail.getTotalPrice()) + "đ");
    }

    @Override
    public int getItemCount() {
        return orderDetails != null ? orderDetails.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgBook;
        TextView tvBookTitle, tvQuantity, tvPrice, tvTotalPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgBook = itemView.findViewById(R.id.imgBook);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
        }
    }
}