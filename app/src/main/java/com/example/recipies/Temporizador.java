package com.example.recipies;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Window;
import android.view.WindowManager;

public class Temporizador extends Fragment {
    private EditText horasEditText;
    private EditText minutosEditText;
    private EditText segundosEditText;
    private TextView tiempoTextView;
    private ProgressBar progressBar;
    private ImageButton empezarButton;
    private ImageButton pausarButton;
    private ImageButton reiniciarButton;
    private CountDownTimer countDownTimer;
    private boolean isRunning = false;
    private long tiempoTotalMillis;
    private long tiempoTranscurrido = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_temporizador, container, false);


        // Obtén la ventana actual y configura la bandera FLAG_KEEP_SCREEN_ON
        Window window = requireActivity().getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        horasEditText = view.findViewById(R.id.horasEditText);
        minutosEditText = view.findViewById(R.id.minutosEditText);
        segundosEditText = view.findViewById(R.id.segundosEditText);
        tiempoTextView = view.findViewById(R.id.texto);
        progressBar = view.findViewById(R.id.progressBar);
        empezarButton = view.findViewById(R.id.empezar);
        pausarButton = view.findViewById(R.id.pausar);
        reiniciarButton = view.findViewById(R.id.reiniciar);

        // Asigna clics a los botones
        empezarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRunning) {
                    pauseTimer();
                } else {
                    if (isValidInput()) {
                        startTimer();
                    } else {
                        Toast.makeText(requireContext(), "Ingresa al menos un valor en horas, minutos o segundos", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        pausarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseTimer();
            }
        });

        reiniciarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTimer();
            }
        });

        return view;
    }

    private boolean isValidInput() {
        return !horasEditText.getText().toString().isEmpty()
                || !minutosEditText.getText().toString().isEmpty()
                || !segundosEditText.getText().toString().isEmpty();
    }

    private void startTimer() {
        int horas = horasEditText.getText().toString().isEmpty() ? 0 : Integer.parseInt(horasEditText.getText().toString());
        int minutos = minutosEditText.getText().toString().isEmpty() ? 0 : Integer.parseInt(minutosEditText.getText().toString());
        int segundos = segundosEditText.getText().toString().isEmpty() ? 0 : Integer.parseInt(segundosEditText.getText().toString());

        tiempoTotalMillis = ((horas * 3600) + (minutos * 60) + segundos) * 1000;

        countDownTimer = new CountDownTimer(tiempoTotalMillis - tiempoTranscurrido, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tiempoTranscurrido += 1000;
                updateCountdownUI(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                isRunning = false;
                horasEditText.setText("");
                minutosEditText.setText("");
                segundosEditText.setText("");
                empezarButton.setImageResource(R.drawable.ic_play);
                pausarButton.setVisibility(View.GONE);
                reiniciarButton.setVisibility(View.VISIBLE);
                playAlarmSound();
                resetTimer();
            }
        }.start();

        isRunning = true;
        empezarButton.setImageResource(R.drawable.ic_pause);
        pausarButton.setVisibility(View.VISIBLE);
        reiniciarButton.setVisibility(View.GONE);
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            isRunning = false;
            empezarButton.setImageResource(R.drawable.ic_play);
            pausarButton.setVisibility(View.GONE);
            reiniciarButton.setVisibility(View.VISIBLE);
        }
    }

    private void resetTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        tiempoTranscurrido = 0;
        updateCountdownUI(tiempoTotalMillis);

        horasEditText.setText("");
        minutosEditText.setText("");
        segundosEditText.setText("");

        tiempoTextView.setText("00:00:00");

        isRunning = false;
        empezarButton.setImageResource(R.drawable.ic_play);
        pausarButton.setVisibility(View.GONE);
        reiniciarButton.setVisibility(View.GONE);
    }

    private void updateCountdownUI(long millisUntilFinished) {
        int horas = (int) (millisUntilFinished / 3600000);
        int minutos = (int) (millisUntilFinished % 3600000) / 60000;
        int segundos = (int) (millisUntilFinished % 60000) / 1000;

        String tiempoTranscurridoStr = String.format("%02d:%02d:%02d", horas, minutos, segundos);
        tiempoTextView.setText(tiempoTranscurridoStr);

        progressBar.setMax((int) tiempoTotalMillis);
        progressBar.setProgress((int) (tiempoTotalMillis - tiempoTranscurrido));

    }

    private void playAlarmSound() {
        // Crea una instancia de MediaPlayer y configúrala para reproducir el sonido.
        MediaPlayer mediaPlayer = MediaPlayer.create(requireContext(), R.raw.tempo);
        mediaPlayer.start();

        // Limpia la instancia del reproductor después de que termine de reproducir el sonido.
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });
    }
}