package com.example.recipies;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Calendar;

import adaptadores.adaptadorViewPagerHora;
import adaptadores.adaptadorViewPagerPlan;

public class Viewpager_PlanHorarios extends AppCompatActivity {


    private ViewPager2 viewPager;
    private adaptadorViewPagerPlan mAdapter;
    private TabLayout tabLayout;
    private String id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewpager_plan_horarios);

        // Evitar que la pantalla se apague
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Calendar cal = Calendar.getInstance();
        int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
        // Verifica si la hora actual es medianoche
        if (hourOfDay >= 0 && hourOfDay <= 4) {
            //servicio que reinicia los datos
            Intent serviceIntent = new Intent(this, BackgroundService.class);
            startService(serviceIntent);
        }

        Intent intent = getIntent();
        id = intent.getStringExtra("perfil");

        Toast.makeText(this, id, Toast.LENGTH_SHORT).show();

        viewPager = findViewById(R.id.viewPagerPlan);
        tabLayout = findViewById(R.id.tabLayoutPlan);
        mAdapter = new adaptadorViewPagerPlan(getSupportFragmentManager(), getLifecycle(),id);
        viewPager.setAdapter(mAdapter);

        // Vincular el TabLayout con el ViewPager
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position + 1) {
                case 1:
                    tab.setText("Plan");
                    tab.setIcon(R.drawable.libro_de_cocina);
                    break;
                case 2:
                    tab.setText("Desayuno");
                    tab.setIcon(R.drawable.sol);
                    break;
                case 3:
                    tab.setText("Comida");
                    tab.setIcon(R.drawable.atardecer);
                    break;
                case 4:
                    tab.setText("Cena");
                    tab.setIcon(R.drawable.noche);
                    break;
            }
        }).attach();

        int textColor = ContextCompat.getColor(this, R.color.black);
        int selectedTextColor = ContextCompat.getColor(this, R.color.white);
        int iconTint = ContextCompat.getColor(this, R.color.black);

        tabLayout.setTabTextColors(textColor, selectedTextColor);
        tabLayout.setTabIconTint(ColorStateList.valueOf(iconTint));
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}