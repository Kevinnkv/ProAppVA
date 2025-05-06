package com.vuongnk.appandroid.activity;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.vuongnk.appandroid.R;
import com.vuongnk.appandroid.fragment.OrderListFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ListOrderActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_order);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sharedPreferences = this.getSharedPreferences("UserPrefs", 0);
        String role = sharedPreferences.getString("role", "user");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(role.equals("user") ? "Đơn hàng của tôi" : "Quản lý đơn hàng");
        }

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

        OrderPagerAdapter pagerAdapter = new OrderPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Chờ xử lý");
                    break;
                case 1:
                    tab.setText("Đang giao");
                    break;
                case 2:
                    tab.setText("Hoàn thành");
                    break;
                case 3:
                    tab.setText("Khác");
                    break;
            }
        }).attach();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static class OrderPagerAdapter extends FragmentStateAdapter {
        private final Context context;

        public OrderPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
            this.context = fragmentActivity;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            int statusResId;
            switch (position) {
                case 0:
                    statusResId = R.string.pending;
                    break;
                case 1:
                    statusResId = R.string.shipping;
                    break;
                case 2:
                    statusResId = R.string.completed;
                    break;
                case 3:
                    statusResId = R.string.canceled;
                    break;
                default:
                    statusResId = R.string.pending;
            }
            return OrderListFragment.newInstance(context.getString(statusResId));
        }

        @Override
        public int getItemCount() {
            return 4;
        }
    }
}
