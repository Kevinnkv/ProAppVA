package com.vuongnk.appandroid.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vuongnk.appandroid.R;
import com.vuongnk.appandroid.helper.FCMHelper;
import com.vuongnk.appandroid.model.Order;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private Context context;
    private List<Order> orderList;
    private OnOrderClickListener listener;
    private String role;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public OrderAdapter(Context context, OnOrderClickListener listener, String role) {
        this.context = context;
        this.orderList = new ArrayList<>();
        this.listener = listener;
        this.role = role;
    }

    public void setOrderList(List<Order> orderList) {
        this.orderList = orderList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView tvOrderId;
        private TextView tvOrderDate;
        private TextView tvOrderStatus;
        private TextView tvOrderTotal;
        private ImageView ivMenu;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
            ivMenu = itemView.findViewById(R.id.ivMenu);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onOrderClick(orderList.get(position));
                }
            });

            if(role.equals("admin")){
                ivMenu.setVisibility(View.VISIBLE);
                ivMenu.setOnClickListener(v -> {
                    PopupMenu popupMenu = new PopupMenu(context, v);
                    popupMenu.getMenuInflater().inflate(R.menu.order_menu, popupMenu.getMenu());

                    popupMenu.setOnMenuItemClickListener(item -> {
                        int id = item.getItemId();
                        Order currentOrder = orderList.get(getAdapterPosition());

                        if (id == R.id.menu_pending) {
                            updateOrderStatus(currentOrder, "PENDING");
                        } else if (id == R.id.menu_shipping) {
                            updateOrderStatus(currentOrder, "SHIPPING");
                        } else if (id == R.id.menu_completed) {
                            updateOrderStatus(currentOrder, "COMPLETED");
                        } else if (id == R.id.menu_canceled) {
                            updateOrderStatus(currentOrder, "CANCELED");
                        }
                        return true;
                    });

                    popupMenu.show();
                });
            }
        }

        private void updateOrderStatus(Order order, String newStatus) {
            DatabaseReference orderRef = FirebaseDatabase.getInstance()
                    .getReference("orders")
                    .child(order.getId());

            // Lấy user token từ Firebase
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(order.getUserId());

            userRef.child("token").get().addOnSuccessListener(snapshot -> {
                String userToken = snapshot.getValue(String.class);
                if (userToken != null) {
                    // Cập nhật trạng thái đơn hàng
                    orderRef.child("status").setValue(newStatus)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(context, "Cập nhật trạng thái thành công",
                                        Toast.LENGTH_SHORT).show();
                                order.setStatus(newStatus);
                                notifyDataSetChanged();

                                // Gửi thông báo đến user
                                String title = "Cập nhật đơn hàng";
                                String message = getNotificationMessage(order.getId(), newStatus);
                                FCMHelper.sendNotification(context, userToken, title, message, order.getUserId());
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context,
                                        "Lỗi khi cập nhật trạng thái: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                }
            });
        }

        private String getNotificationMessage(String orderId, String status) {
            String statusText;
            switch (status) {
                case "PENDING":
                    statusText = "chờ xử lý";
                    break;
                case "SHIPPING":
                    statusText = "đang giao hàng";
                    break;
                case "COMPLETED":
                    statusText = "đã hoàn thành";
                    break;
                case "CANCELED":
                    statusText = "đã bị hủy";
                    break;
                default:
                    statusText = status;
            }
            return "Đơn hàng " + orderId + " của bạn đã được cập nhật sang trạng thái " + statusText;
        }

        public void bind(Order order) {
            tvOrderId.setText("Mã: " + order.getId());

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String date = sdf.format(new Date(order.getCreatedAt()));
            tvOrderDate.setText("Ngày đặt: " + date);

            String statusText = "";
            switch (order.getStatus()) {
                case "PENDING":
                    statusText = "Chờ xử lý";
                    break;
                case "SHIPPING":
                    statusText = "Đang giao";
                    break;
                case "COMPLETED":
                    statusText = "Hoàn thành";
                    break;
                case "CANCELED":
                    statusText = "Hủy";
                    break;
                default:
                    statusText = order.getStatus();
            }
            tvOrderStatus.setText("Trạng thái: " + statusText);

            tvOrderTotal.setText(String.format("Tổng tiền: %.0f VND", order.getTotalAmount()));
        }
    }
}
