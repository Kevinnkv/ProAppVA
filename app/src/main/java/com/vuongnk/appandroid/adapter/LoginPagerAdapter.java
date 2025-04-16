package com.vuongnk.appandroid.adapter;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.vuongnk.appandroid.fragment.LoginFragment;
import com.vuongnk.appandroid.fragment.RegisterFragment;
public class LoginPagerAdapter extends FragmentStateAdapter {
    public LoginPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new LoginFragment();
        }
        return new RegisterFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}