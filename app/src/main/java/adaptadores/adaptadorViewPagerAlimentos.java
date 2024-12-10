package adaptadores;

import android.support.annotation.NonNull;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.recipies.Alacena;
import com.example.recipies.Congelador;
import com.example.recipies.Crud_Fruits_Vegetables_Fragment;
import com.example.recipies.Crud_Products_Fragment;
import com.example.recipies.Crud_Recipies_Fragment;
import com.example.recipies.Refrigerador;

public class adaptadorViewPagerAlimentos extends FragmentStateAdapter {

    private static final int NUM_PAGES = 3;

    public adaptadorViewPagerAlimentos(FragmentManager fragmentManager, Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new Refrigerador();
            case 1:
                return new Alacena();
            case 2:
                return new Congelador();
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }
}
