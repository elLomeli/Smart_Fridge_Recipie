package com.example.recipies;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ProductExpirationWorker extends Worker {

    private FirebaseFirestore db;
    private String email;
    private List<String> productosCercaDeCaducar = new ArrayList<>();

    public ProductExpirationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        db = FirebaseFirestore.getInstance();
        SharedPreferences sharedPrefs = context.getSharedPreferences("correo", Context.MODE_PRIVATE);
        email = sharedPrefs.getString("email", null);
    }

    @NonNull
    @Override
    public Result doWork() {
        obtenerProductosCercaDeCaducar();
        return Result.success();
    }

    private void obtenerProductosCercaDeCaducar() {
        db.collection(email + " Productos")
                .whereLessThanOrEqualTo("Caducidad", 5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productosCercaDeCaducar.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String nombreProducto = document.getString("Nombre");
                        if (nombreProducto != null) {
                            productosCercaDeCaducar.add(nombreProducto);
                            Log.d("ProductExpirationWorker", "Producto cercano a caducar: " + nombreProducto);
                            enviarNotificacion(nombreProducto);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("ProductExpirationWorker", "Error al obtener productos cerca de caducar", e));
    }

    private void enviarNotificacion(String producto) {
        // Crear canal de notificación para Android 8.0 y superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "PRODUCT_EXPIRATION_CHANNEL",
                    "Producto a punto de caducar",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        // Intent para abrir la actividad MenosSobras al hacer clic en la notificación
        Intent intent = new Intent(getApplicationContext(), Menu.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Crear la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "PRODUCT_EXPIRATION_CHANNEL")
                .setSmallIcon(R.drawable.notificacion) // Icono para la notificación
                .setContentTitle("Producto a punto de caducar")
                .setContentText("El producto \"" + producto + "\" está a punto de caducar. Revisa en qué recetas lo puedes usar.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Enviar la notificación
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(producto.hashCode(), builder.build());
        }
    }
}
