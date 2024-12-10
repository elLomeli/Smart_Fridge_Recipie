package adaptadores;

import android.support.annotation.NonNull;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.recipies.Crud_Fruits_Vegetables_Fragment;
import com.example.recipies.Crud_Products_Fragment;
import com.example.recipies.Crud_Recipies_Fragment;

public class adaptadorViewPagerCruds extends FragmentStateAdapter {

    private static final int NUM_PAGES = 3;

    public adaptadorViewPagerCruds(FragmentManager fragmentManager, Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new Crud_Products_Fragment();
            case 1:
                return new Crud_Fruits_Vegetables_Fragment();
            case 2:
                return new Crud_Recipies_Fragment();
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }
}
