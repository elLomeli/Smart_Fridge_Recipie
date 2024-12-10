package com.example.recipies;



import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.view.Window;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class Cronometro extends Fragment {
    private TextView tiempoTextView;
    private ImageButton empezarButton;
    private ImageButton reiniciarButton;
    private CountDownTimer countDownTimer;
    private boolean isRunning = false;
    private long tiempoTranscurrido = 0;

    public Cronometro() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_cronometro, container, false);

        // Obtén la ventana actual y configura la bandera FLAG_KEEP_SCREEN_ON
        Window window = requireActivity().getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        tiempoTextView = view.findViewById(R.id.texto);
        empezarButton = view.findViewById(R.id.empezar);
        reiniciarButton = view.findViewById(R.id.reiniciar);

        empezarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRunning) {
                    pauseTimer();
                } else {
                    startTimer();
                }
            }
        });

        reiniciarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTimer();
            }
        });

        // Restaurar el estado del cronómetro si es necesario
        if (savedInstanceState != null) {
            isRunning = savedInstanceState.getBoolean("isRunning");
            tiempoTranscurrido = savedInstanceState.getLong("tiempoTranscurrido");
            if (isRunning) {
                startTimer();
            } else {
                updateCountdownUI();
            }
        }

        return view;
    }

    private void startTimer() {
        if (!isRunning) {
            countDownTimer = new CountDownTimer(Long.MAX_VALUE, 1000) {  // Contador infinito
                @Override
                public void onTick(long millisUntilFinished) {
                    tiempoTranscurrido += 1000;  // Incrementa el tiempo en 1 segundo
                    updateCountdownUI();
                }

                @Override
                public void onFinish() {
                    // No debería ocurrir con un contador infinito
                }
            }.start();

            isRunning = true;
            empezarButton.setImageResource(R.drawable.ic_pause);
        }
    }

    private void pauseTimer() {
        if (isRunning) {
            countDownTimer.cancel();
            isRunning = false;
            empezarButton.setImageResource(R.drawable.ic_play);
        }
    }

    private void resetTimer() {
        if (isRunning) {
            countDownTimer.cancel();
        }

        tiempoTranscurrido = 0;
        updateCountdownUI();
        isRunning = false;
        empezarButton.setImageResource(R.drawable.ic_play);
    }

    private void updateCountdownUI() {
        int segundos = (int) (tiempoTranscurrido / 1000) % 60;
        int minutos = (int) ((tiempoTranscurrido / (1000 * 60)) % 60);
        int horas = (int) ((tiempoTranscurrido / (1000 * 60 * 60)) % 24);

        String tiempoTranscurridoStr = String.format("%02d:%02d:%02d", horas, minutos, segundos);
        tiempoTextView.setText(tiempoTranscurridoStr);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isRunning", isRunning);
        outState.putLong("tiempoTranscurrido", tiempoTranscurrido);
    }
}




