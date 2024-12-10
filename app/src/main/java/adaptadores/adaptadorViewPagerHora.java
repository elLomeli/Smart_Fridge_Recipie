package adaptadores;

import android.support.annotation.NonNull;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.recipies.Cronometro;
import com.example.recipies.Crud_Fruits_Vegetables_Fragment;
import com.example.recipies.Crud_Products_Fragment;
import com.example.recipies.Crud_Recipies_Fragment;
import com.example.recipies.Temporizador;

public class adaptadorViewPagerHora extends FragmentStateAdapter {

    private static final int NUM_PAGES = 2;

    public adaptadorViewPagerHora(FragmentManager fragmentManager, Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new Cronometro();
            case 1:
                return new Temporizador();
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }
}
