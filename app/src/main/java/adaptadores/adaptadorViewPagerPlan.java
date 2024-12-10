package adaptadores;

import android.support.annotation.NonNull;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.recipies.PlanPerfil;
import com.example.recipies.RecetasPlanAlimenticioCena;
import com.example.recipies.RecetasPlanAlimenticioComida;
import com.example.recipies.RecetasPlanAlimenticioDesayuno;
import com.example.recipies.RecetasPreparadas;

public class adaptadorViewPagerPlan extends FragmentStateAdapter {

    private static final int NUM_PAGES = 4;
    private String id; // Variable para almacenar el id

    public adaptadorViewPagerPlan(FragmentManager fragmentManager, Lifecycle lifecycle, String id) {
        super(fragmentManager, lifecycle);
        this.id = id;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new PlanPerfil().newInstance(id);
            case 1:
                return new RecetasPlanAlimenticioDesayuno().newInstance(id);
            case 2:
                return new RecetasPlanAlimenticioComida().newInstance(id);
            case 3:
                return new RecetasPlanAlimenticioCena().newInstance(id);
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }
}
