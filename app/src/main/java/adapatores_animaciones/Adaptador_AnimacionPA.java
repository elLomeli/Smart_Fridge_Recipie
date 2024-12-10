package adapatores_animaciones;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.airbnb.lottie.LottieAnimationView;
import com.example.recipies.PlanAlimenticio;
import com.example.recipies.R;

public class Adaptador_AnimacionPA implements Animator.AnimatorListener{

    private final LottieAnimationView animationView;
    private final Context context;
    private boolean actionCompleted = false;

    public Adaptador_AnimacionPA(LottieAnimationView animationView, Context context) {
        this.animationView = animationView;
        this.context = context;
    }

    @Override
    public void onAnimationStart(@NonNull Animator animator) {

    }

    @Override
    public void onAnimationEnd(@NonNull Animator animator) {
        animationView.setEnabled(true);
        // Abre la actividad adicional solo si la acción no se ha completado antes
        if (!actionCompleted) {
            Intent intent = new Intent(context,PlanAlimenticio.class);
            context.startActivity(intent);
            actionCompleted = true; // Marcar la acción como completada
        }
        animationView.setImageResource(R.drawable.cocina);
    }

    @Override
    public void onAnimationCancel(@NonNull Animator animator) {

    }

    @Override
    public void onAnimationRepeat(@NonNull Animator animator) {

    }
}
