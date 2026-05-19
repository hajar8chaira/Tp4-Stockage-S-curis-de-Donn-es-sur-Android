package com.example.securestorageapp.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.securestorageapp.fragments.DatabaseFragment;
import com.example.securestorageapp.fragments.EncryptedFilesFragment;
import com.example.securestorageapp.fragments.ExternalStorageFragment;
import com.example.securestorageapp.fragments.InternalStorageFragment;
import com.example.securestorageapp.fragments.SecurityRisksFragment;

public class MainPagerAdapter extends FragmentStateAdapter {

    private final int userId;
    private final String username;

    public MainPagerAdapter(@NonNull FragmentActivity fragmentActivity, int userId, String username) {
        super(fragmentActivity);
        this.userId = userId;
        this.username = username;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new InternalStorageFragment();
            case 1:
                return new ExternalStorageFragment();
            case 2:
                return DatabaseFragment.newInstance(userId, username);
            case 3:
                return new EncryptedFilesFragment();
            default:
                return new SecurityRisksFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}
