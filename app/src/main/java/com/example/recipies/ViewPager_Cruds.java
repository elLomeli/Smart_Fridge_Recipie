package com.example.recipies;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.core.graphics.drawable.DrawableCompat;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;

import adaptadores.adaptadorViewPagerCruds;

public class ViewPager_Cruds extends AppCompatActivity {

    private ViewPager2 viewPager;
    private adaptadorViewPagerCruds mAdapter;
    private TabLayout tabLayout;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager_cruds);

        // Evitar que la pantalla se apague
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        mAuth = FirebaseAuth.getInstance();
        mAdapter = new adaptadorViewPagerCruds(getSupportFragmentManager(), getLifecycle());
        viewPager.setAdapter(mAdapter);

        // Vincular el TabLayout con el ViewPager
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position + 1) {
                case 1:
                    tab.setText("Productos");
                    tab.setIcon(R.drawable.productos);
                    break;
                case 2:
                    tab.setText("F & V");
                    tab.setIcon(R.drawable.frutasverduras);
                    break;
                case 3:
                    tab.setText("Recetas");
                    tab.setIcon(R.drawable.recipies_foreground);
                    break;
            }
        }).attach();

        int textColor = ContextCompat.getColor(this, R.color.black);
        int selectedTextColor = ContextCompat.getColor(this, R.color.white);
        int iconTint = ContextCompat.getColor(this, R.color.black);

        tabLayout.setTabTextColors(textColor, selectedTextColor);
        tabLayout.setTabIconTint(ColorStateList.valueOf(iconTint));

        // Configurar el Navigation Drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            // Manejar los clics en los elementos del Navigation Drawer
            switch (item.getItemId()) {
                case R.id.LogoutConfigProducts: {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("¿Está seguro de que desea cerrar sesión?")
                            .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Toast.makeText(ViewPager_Cruds.this, "Sesión Cerrada", Toast.LENGTH_SHORT).show();
                                    mAuth.signOut();
                                    startActivity(new Intent(ViewPager_Cruds.this, Login.class));
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Toast.makeText(ViewPager_Cruds.this, "Sesión no Cerrada", Toast.LENGTH_SHORT).show();
                                }
                            });
                    builder.create().show();
                    return true;
                }
                default:
                    return false;
            }
        });
    }

    // método para manejar el comportamiento del Navigation Drawer
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // método para cerrar el Navigation Drawer al presionar el botón "Atrás"
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}



