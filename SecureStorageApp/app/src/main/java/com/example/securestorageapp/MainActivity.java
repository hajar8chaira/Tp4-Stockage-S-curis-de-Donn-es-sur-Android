package com.example.securestorageapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.example.securestorageapp.adapters.MainPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int userId = getIntent().getIntExtra("USER_ID", -1);
        String username = getIntent().getStringExtra("USERNAME");

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);

        MainPagerAdapter pagerAdapter = new MainPagerAdapter(this, userId, username);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Interne");
                    break;
                case 1:
                    tab.setText("Externe");
                    break;
                case 2:
                    tab.setText("BDD");
                    break;
                case 3:
                    tab.setText("Jetpack");
                    break;
                case 4:
                    tab.setText("Sécurité");
                    break;
            }
        }).attach();
    }
}
