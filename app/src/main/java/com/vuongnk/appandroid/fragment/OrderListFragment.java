package com.vuongnk.appandroid.fragment;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vuongnk.appandroid.R;
import com.vuongnk.appandroid.activity.OrderDetailActivity;
import com.vuongnk.appandroid.adapter.OrderAdapter;
import com.vuongnk.appandroid.model.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderListFragment extends Fragment implements OrderAdapter.OnOrderClickListener {
    private static final String ARG_STATUS = "status";
    private String status;
    private RecyclerView recyclerView;
    private OrderAdapter adapter;
    private View layoutEmpty;
    private SharedPreferences sharedPreferences;
    private String role;

    public static OrderListFragment newInstance(String status) {
        OrderListFragment fragment = new OrderListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STATUS, status);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            status = getArguments().getString(ARG_STATUS);
        }
        sharedPreferences = requireContext().getSharedPreferences("UserPrefs", 0);
        role = sharedPreferences.getString("role", "user");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new OrderAdapter(getContext(), this, role);
        recyclerView.setAdapter(adapter);

        loadOrders();

        return view;
    }

    private void loadOrders() {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders");
        Query ordersQuery;


        if (role.equals("admin")) {
            // Nếu là admin thì lấy tất cả đơn hàng
            ordersQuery = ordersRef;
        } else {
            // Nếu là user thường thì chỉ lấy đơn hàng của user đó
            ordersQuery = ordersRef.orderByChild("userId")
                    .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        }

        ordersQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Order> orders = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Order order = snapshot.getValue(Order.class);
                    if (order != null && order.getStatus().equals(status)) {
                        order.setId(snapshot.getKey());
                        orders.add(order);
                    }
                }

                Collections.sort(orders, (o1, o2) -> Long.compare(o2.getCreatedAt(), o1.getCreatedAt()));
                adapter.setOrderList(orders);

                // Hiển thị/ẩn view trống dựa vào số lượng đơn hàng
                if (orders.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    layoutEmpty.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi khi tải đơn hàng: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onOrderClick(Order order) {
        Intent intent = new Intent(getContext(), OrderDetailActivity.class);
        intent.putExtra("order", order);
        startActivity(intent);
    }
}
