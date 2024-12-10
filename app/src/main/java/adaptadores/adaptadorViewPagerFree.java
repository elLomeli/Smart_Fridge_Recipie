package adaptadores;

import android.support.annotation.NonNull;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.recipies.MenosSobras;
import com.example.recipies.Recetas;

public class adaptadorViewPagerFree extends FragmentStateAdapter {

    private static final int NUM_PAGES = 2;

    public adaptadorViewPagerFree(FragmentManager fragmentManager, Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new Recetas();
            case 1:
                return new MenosSobras();
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }
}
