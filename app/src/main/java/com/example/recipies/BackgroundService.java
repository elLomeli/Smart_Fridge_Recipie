package com.example.recipies;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import java.util.Calendar;

public class BackgroundService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
            resetValues();

        // Devuelve el tipo de comportamiento del servicio
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // No necesitamos enlace para este servicio
        return null;
    }

    private void resetValues() {
        for (int i = 1; i <= 4; i++) {
            resetSharedPreferences("EmpezarRecetaDesayuno" + i);
            resetSharedPreferences("EmpezarRecetaComida" + i);
            resetSharedPreferences("EmpezarRecetaCena" + i);
            resetCaloriesSharedPreferences(i);
        }
    }

    private void resetSharedPreferences(String key) {
        SharedPreferences prefs = getSharedPreferences(key, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("startedToday", false);
        editor.apply();
    }

    private void resetCaloriesSharedPreferences(int profileId) {
        SharedPreferences prefs = getSharedPreferences("CaloriasRestantes" + profileId, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }
}