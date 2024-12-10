package com.example.recipies;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;

import adaptadores.adaptadorViewPagerAlimentos;



public class Alimentos extends AppCompatActivity {

    private ViewPager2 viewPager;
    private adaptadorViewPagerAlimentos mAdapter;
    private TabLayout tabLayout;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alimentos);

        // Evitar que la pantalla se apague
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        mAdapter = new adaptadorViewPagerAlimentos(getSupportFragmentManager(), getLifecycle());
        viewPager.setAdapter(mAdapter);

        // Vincular el TabLayout con el ViewPager
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position + 1) {
                case 1:
                    tab.setText("Refrigerador");
                    tab.setIcon(R.drawable.nevera);
                    break;
                case 2:
                    tab.setText("Alacena");
                    tab.setIcon(R.drawable.estantes_de_cocina);
                    break;
                case 3:
                    tab.setText("Congelador");
                    tab.setIcon(R.drawable.congelado);
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