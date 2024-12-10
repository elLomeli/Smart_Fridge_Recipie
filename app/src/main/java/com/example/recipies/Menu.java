package com.example.recipies;

import android.animation.Animator;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import Correo.JavaMailAPI;
import adapatores_animaciones.Adaptador_AnimacionAlimentos;
import adapatores_animaciones.Adaptador_AnimacionConfiguracion;
import adapatores_animaciones.Adaptador_AnimacionPA;

public class Menu extends AppCompatActivity {
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_PERMISSION_BLUETOOTH = 100;

    private ImageView bluetoothButton;
    private TextView textViewBluetoothData;
    private LottieAnimationView animationConfiguracion, animationPlan, animationAlimento, animationFree, animationCalendario, animationTemperatura;
    private TextView dateTextView, horaTextView;
    private Handler handler;
    private LinearLayout linearLayoutTemp;
    private String temperaturaStr;
    private String humedad;
    private BluetoothService bluetoothService;
    private boolean isBound = false;
    private boolean correoEnviado = false;
    private int tiempoActual = 0;
    private double temperaturaAnterior = Double.MIN_VALUE; // Valor inicial para la comparación de temperatura
    private static final int INTERVALO_TEMPORIZADOR = 1000; // Intervalo en milisegundos
    private static final int TIEMPO_LIMITE = 1 * 60; // 1 minuto
    private String email;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            bluetoothService = binder.getService();
            isBound = true;
            updateBluetoothButton(bluetoothService.isBluetoothConnected()); // Actualizar el botón al conectar el servicio
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            updateBluetoothButton(false); // Cambiar imagen a desconectado cuando se desconecta el servicio
        }
    };

    public final BroadcastReceiver connectionStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case "BluetoothConnectionStatus":
                        boolean isConnected = intent.getBooleanExtra("isConnected", false);
                        updateBluetoothButton(isConnected);
                        break;
                    case "BluetoothData":
                        temperaturaStr = intent.getStringExtra("temperatura");
                        humedad = intent.getStringExtra("humedad");
                        updateTemperatureData();
                        break;
                }
            }
        }
    };

    // Método para actualizar la imagen del botón Bluetooth
    public void updateBluetoothButton(boolean isConnected) {
        runOnUiThread(() -> {
            if (isConnected) {
                bluetoothButton.setImageResource(R.drawable.bluetooth_on);
            } else {
                bluetoothButton.setImageResource(R.drawable.bluetooth_off);
            }
        });
    }

    // Método para actualizar los datos de temperatura en la UI
    private void updateTemperatureData() {
        runOnUiThread(() -> {
            if (temperaturaStr != null && !temperaturaStr.isEmpty()) {
                try {
                    int temperatura = Integer.parseInt(temperaturaStr);
                    textViewBluetoothData.setText(temperatura + " °C");
                    // Aquí puedes llamar a otro método para actualizar la UI basada en la temperatura, si es necesario
                    updateTemperatureUI(temperatura);
                } catch (NumberFormatException e) {
                    // Manejar error en la conversión de la temperatura
                    e.printStackTrace();
                }
            }
        });
    }

    // Método para actualizar el diseño basado en la temperatura (colores, animaciones, etc.)
    private void updateTemperatureUI(int temperatura) {
        if (temperatura < 1) {
            animationTemperatura.setAnimation(R.raw.congelando);
            linearLayoutTemp.setBackgroundColor(ContextCompat.getColor(Menu.this, R.color.Congelando));
        } else if (temperatura >= 1 && temperatura <= 6) {
            animationTemperatura.setAnimation(R.raw.termometrofrio);
            linearLayoutTemp.setBackgroundColor(ContextCompat.getColor(Menu.this, R.color.Perfecto));
        } else if (temperatura >= 7 && temperatura <= 15) {
            animationTemperatura.setAnimation(R.raw.termometrocaliente);
            linearLayoutTemp.setBackgroundColor(ContextCompat.getColor(Menu.this, R.color.Aceptable));
        } else {
            animationTemperatura.setAnimation(R.raw.termometrocaliente);
            linearLayoutTemp.setBackgroundColor(ContextCompat.getColor(Menu.this, R.color.Caliente));
        }
        animationTemperatura.setRepeatCount(LottieDrawable.INFINITE);
        animationTemperatura.playAnimation();
    }

    private void configurarWorkManager2() {
        // Definir un nombre único para el trabajo
        String uniqueWorkName = "ActualizarCaducidadProductosDiario";

        // Calcular el tiempo de retraso inicial hasta la 1 AM
        long currentTimeMillis = System.currentTimeMillis();
        long oneAMMillis = getOneAMMillis();
        long initialDelay = oneAMMillis - currentTimeMillis;

        if (initialDelay < 0) {
            // Si la 1 AM de hoy ya pasó, programamos para la 1 AM del día siguiente
            initialDelay += TimeUnit.DAYS.toMillis(1);
        }

        // Configurar una solicitud de trabajo periódico que se ejecute cada 24 horas a partir de la 1 AM
        PeriodicWorkRequest caducidadWorkRequest = new PeriodicWorkRequest.Builder(
                ProductExpirationWorker.class, 12, TimeUnit.HOURS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .build();

        // Programar el trabajo único (solo se ejecutará una vez si ya está programado)
        WorkManager.getInstance(this)
                .enqueueUniquePeriodicWork(
                        uniqueWorkName,
                        ExistingPeriodicWorkPolicy.KEEP, // Mantiene el trabajo existente si ya está encolado
                        caducidadWorkRequest);
    }

    private void configurarWorkManager() {
        PeriodicWorkRequest expirationWorkRequest = new PeriodicWorkRequest.Builder(
                CaducidadWorker.class, 24, TimeUnit.HOURS)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "ProductExpirationWork",
                ExistingPeriodicWorkPolicy.KEEP, // Mantener el trabajo si ya existe
                expirationWorkRequest);
        // Definir un nombre único para el trabajo
        String uniqueWorkName = "ActualizarCaducidadProductosDiario";

        // Calcular el tiempo de retraso inicial hasta la 1 AM
        long currentTimeMillis = System.currentTimeMillis();
        long oneAMMillis = getOneAMMillis();
        long initialDelay = oneAMMillis - currentTimeMillis;

        if (initialDelay < 0) {
            // Si la 1 AM de hoy ya pasó, programamos para la 1 AM del día siguiente
            initialDelay += TimeUnit.DAYS.toMillis(1);
        }

        // Configurar una solicitud de trabajo periódico que se ejecute cada 24 horas a partir de la 1 AM
        PeriodicWorkRequest caducidadWorkRequest = new PeriodicWorkRequest.Builder(
                CaducidadWorker.class, 24, TimeUnit.HOURS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .build();

        // Programar el trabajo único (solo se ejecutará una vez si ya está programado)
        WorkManager.getInstance(this)
                .enqueueUniquePeriodicWork(
                        uniqueWorkName,
                        ExistingPeriodicWorkPolicy.KEEP, // Mantiene el trabajo existente si ya está encolado
                        caducidadWorkRequest);
    }

    // Método para calcular la hora de las 1 AM en milisegundos del día actual
    private long getOneAMMillis() {
        // Configurar la fecha y hora para las 1 AM del día actual
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 1);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // Inicializar vistas
        bluetoothButton = findViewById(R.id.BluetoothButton);
        textViewBluetoothData = findViewById(R.id.textViewBluetoothdata);
        animationConfiguracion = findViewById(R.id.Configuracion);
        animationPlan = findViewById(R.id.plan);
        animationAlimento = findViewById(R.id.alimentos);
        animationFree = findViewById(R.id.recetas);
        animationCalendario = findViewById(R.id.calendario);
        animationTemperatura = findViewById(R.id.animacionTemp);
        dateTextView = findViewById(R.id.dateTextView);
        horaTextView = findViewById(R.id.hora);
        linearLayoutTemp = findViewById(R.id.LinearLayoutTemp);

        handler = new Handler(Looper.getMainLooper());

        // Solicitar permisos de Bluetooth en tiempo de ejecución para Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        android.Manifest.permission.BLUETOOTH_CONNECT,
                        android.Manifest.permission.BLUETOOTH_SCAN
                }, REQUEST_PERMISSION_BLUETOOTH);
            }
        }

        // Botón Bluetooth
        bluetoothButton.setOnClickListener(view -> {
            if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                Intent intent = new Intent(Menu.this, Dispositivos_Vinculados.class);
                startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
            }
        });

        // Inicializar animaciones
        initAnimations();

        //resataodr de caducidad
        configurarWorkManager();
        //caducidad notificaciones
        configurarWorkManager2();


        // Iniciar el servicio Bluetooth
        SharedPreferences sharedPrefs = getSharedPreferences("correo", Context.MODE_PRIVATE);
        email = sharedPrefs.getString("email", null);
        Intent bluetoothServiceIntent = new Intent(this, BluetoothService.class);
        bluetoothServiceIntent.putExtra("email", email);
        startService(bluetoothServiceIntent);
        bindService(bluetoothServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        // Registrar receptor de datos de Bluetooth
        IntentFilter dataFilter = new IntentFilter();
        dataFilter.addAction("BluetoothConnectionStatus");
        dataFilter.addAction("BluetoothData");
        registerReceiver(connectionStatusReceiver, dataFilter);

        // Mostrar hora y fecha actual
        updateDate();
        updateHour();
        scheduleHourUpdate();
        scheduleDailyUpdate();
    }


    // Método para inicializar y manejar las animaciones
    private void initAnimations() {
        animationCalendario.setRepeatCount(LottieDrawable.INFINITE);
        animationCalendario.playAnimation();

        animationFree.setRepeatCount(LottieDrawable.INFINITE);
        animationFree.playAnimation();

        animationConfiguracion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Desactivar el botón mientras la animación está en curso
                animationConfiguracion.setEnabled(false);
                // Mostrar la animación cuando se hace clic en el botón
                animationConfiguracion.setVisibility(View.VISIBLE);
                animationConfiguracion.setAnimation(R.raw.configuracion);
                animationConfiguracion.playAnimation();
                animationConfiguracion.addAnimatorListener(new Adaptador_AnimacionConfiguracion(animationConfiguracion, Menu.this));
            }
        });
        animationPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Desactivar el botón mientras la animación está en curso
                animationPlan.setEnabled(false);
                // Mostrar la animación cuando se hace clic en el botón
                animationPlan.setVisibility(View.VISIBLE);
                animationPlan.setAnimation(R.raw.planalimenticio);
                animationPlan.playAnimation();
                animationPlan.addAnimatorListener(new Adaptador_AnimacionPA(animationPlan, Menu.this));
            }
        });
        // Verificar conexión Bluetooth cuando se hace clic en animationAlimento
        animationAlimento.setOnClickListener(view -> {
            if (bluetoothService != null && bluetoothService.isBluetoothConnected()) {
                // Desactivar el botón mientras la animación está en curso
                animationAlimento.setEnabled(false);
                // Mostrar la animación cuando se hace clic en el botón
                animationAlimento.setVisibility(View.VISIBLE);
                animationAlimento.setAnimation(R.raw.alimentos);
                animationAlimento.playAnimation();
                animationAlimento.addAnimatorListener(new Adaptador_AnimacionAlimentos(animationAlimento, Menu.this));
            } else {
                // Si no está conectado, mostrar un mensaje de advertencia
                Toast.makeText(this, "Debe conectar el Bluetooth para ver esta información", Toast.LENGTH_SHORT).show();
            }
        });

        animationFree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Menu.this, FreeRecipies.class);
                startActivity(intent);
            }
        });
        horaTextView.setOnClickListener(view -> {
            Intent intent = new Intent(Menu.this, ViewPager_Hora.class);
            startActivity(intent);
        });

        animationCalendario.setOnClickListener(view -> {
            Intent intent = new Intent(Menu.this, Calendario.class);
            startActivity(intent);
        });
    }

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("BluetoothData")) {
                temperaturaStr = intent.getStringExtra("temperatura");
                humedad = intent.getStringExtra("humedad");
                updateUI();
            }
        }
    };

    private void updateUI() {
        if (temperaturaStr != null && !temperaturaStr.isEmpty()) {
            try {
                int temperatura = Integer.parseInt(temperaturaStr);
                textViewBluetoothData.setText(temperatura + " °C");
                if (temperatura != temperaturaAnterior) {
                    if (temperatura < 1) {
                        animationTemperatura.setAnimation(R.raw.congelando);
                        linearLayoutTemp.setBackgroundColor(ContextCompat.getColor(Menu.this, R.color.Congelando));
                    } else if (temperatura >= 1 && temperatura <= 6) {
                        animationTemperatura.setAnimation(R.raw.termometrofrio);
                        linearLayoutTemp.setBackgroundColor(ContextCompat.getColor(Menu.this, R.color.Perfecto));
                    } else if (temperatura >= 7 && temperatura <= 15) {
                        animationTemperatura.setAnimation(R.raw.termometrocaliente);
                        linearLayoutTemp.setBackgroundColor(ContextCompat.getColor(Menu.this, R.color.Aceptable));
                    } else {
                        animationTemperatura.setAnimation(R.raw.termometrocaliente);
                        linearLayoutTemp.setBackgroundColor(ContextCompat.getColor(Menu.this, R.color.Caliente));
                    }

                    animationTemperatura.setRepeatCount(LottieDrawable.INFINITE);
                    animationTemperatura.playAnimation();
                    temperaturaAnterior = temperatura;

                    handler.postDelayed(() -> {
                        if (temperatura == temperaturaAnterior) {
                            tiempoActual += INTERVALO_TEMPORIZADOR / 1000;
                        } else {
                            tiempoActual = 0;
                        }

                        if (tiempoActual >= TIEMPO_LIMITE && !correoEnviado) {
                            enviarCorreo();
                            AlertDialog.Builder builder = new AlertDialog.Builder(Menu.this);
                            builder.setMessage("La temperatura de tu refrigerador está muy alta")
                                    .setPositiveButton("Aceptar", (dialog, id) -> dialog.dismiss());
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            correoEnviado = true;
                        }

                    }, INTERVALO_TEMPORIZADOR);
                }
            } catch (NumberFormatException e) {
                // Manejar error en la conversión de la temperatura
            }
        }
    }

    private void enviarCorreo() {
        String subject = "Alerta de temperatura";
        String message = "La temperatura de tu refrigerador está fuera del rango aceptable. Verifica el estado.";
        JavaMailAPI javaMailAPI = new JavaMailAPI(this, email, subject, message);
        javaMailAPI.execute();
    }

    private void updateDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String formattedDate = sdf.format(new Date());
        dateTextView.setText(formattedDate);
    }

    private void updateHour() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String formattedHour = sdf.format(new Date());
        horaTextView.setText(formattedHour);
    }

    private void scheduleHourUpdate() {
        handler.postDelayed(() -> {
            updateHour();
            scheduleHourUpdate();
        }, 1000);
    }

    private void scheduleDailyUpdate() {
        handler.postDelayed(() -> {
            updateDate();
            scheduleDailyUpdate();
        }, getNextMidnightMillis());
    }

    private long getNextMidnightMillis() {
        long currentTimeMillis = System.currentTimeMillis();
        long nextMidnightMillis = ((currentTimeMillis / 86400000) + 1) * 86400000;
        return nextMidnightMillis - currentTimeMillis;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CONNECT_DEVICE && resultCode == RESULT_OK) {
            String deviceAddress = data.getStringExtra("DEVICE_ADDRESS");
            if (isBound) {
                bluetoothService.connectToBluetoothDevice(deviceAddress);
            }
        } else if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            Intent intent = new Intent(Menu.this, Dispositivos_Vinculados.class);
            startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
        } else {
            Toast.makeText(this, "Bluetooth no habilitado", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
        unregisterReceiver(connectionStatusReceiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_BLUETOOTH) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido
            } else {
                Toast.makeText(this, "Permiso para Bluetooth denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

