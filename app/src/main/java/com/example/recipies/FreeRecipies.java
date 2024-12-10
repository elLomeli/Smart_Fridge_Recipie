package com.example.recipies;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import adaptadores.adaptadorViewPagerFree;

public class FreeRecipies extends AppCompatActivity {


    private ViewPager2 viewPager;
    private adaptadorViewPagerFree mAdapter;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_recipies);

        // Evitar que la pantalla se apague
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        viewPager = findViewById(R.id.viewPagerFree);
        tabLayout = findViewById(R.id.tabLayoutFree);
        mAdapter = new adaptadorViewPagerFree(getSupportFragmentManager(), getLifecycle());
        viewPager.setAdapter(mAdapter);

        // Vincular el TabLayout con el ViewPager
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position + 1) {
                case 1:
                    tab.setText("Free Recipies");
                    tab.setIcon(R.drawable.libro_de_cocina);
                    break;
                case 2:
                    tab.setText("Menos Sobras");
                    tab.setIcon(R.drawable.sobras);
                    break;
            }
        }).attach();

        int textColor = ContextCompat.getColor(this, R.color.black);
        int selectedTextColor = ContextCompat.getColor(this, R.color.white);
        int iconTint = ContextCompat.getColor(this, R.color.black);

        tabLayout.setTabTextColors(textColor, selectedTextColor);
        tabLayout.setTabIconTint(ColorStateList.valueOf(iconTint));
    }
}