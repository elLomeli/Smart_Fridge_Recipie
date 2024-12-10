package com.example.recipies;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.WindowManager;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;

import adaptadores.adaptadorViewPagerHora;

public class ViewPager_Hora extends AppCompatActivity {
    private ViewPager2 viewPager;
    private adaptadorViewPagerHora mAdapter;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager_hora);
        // Evitar que la pantalla se apague
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        viewPager = findViewById(R.id.viewPagerH);
        tabLayout = findViewById(R.id.tabLayoutH);
        mAdapter = new adaptadorViewPagerHora(getSupportFragmentManager(), getLifecycle());
        viewPager.setAdapter(mAdapter);


        // Vincular el TabLayout con el ViewPager
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position + 1) {
                case 1:
                    tab.setText("Cronometro");
                    tab.setIcon(R.drawable.cronografo);
                    break;
                case 2:
                    tab.setText("Temporizador");
                    tab.setIcon(R.drawable.reloj_de_arena);
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