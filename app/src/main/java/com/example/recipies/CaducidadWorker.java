package com.example.recipies;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class CaducidadWorker extends Worker {

    private FirebaseFirestore db;

    public CaducidadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        db = FirebaseFirestore.getInstance(); // Inicializa Firestore
    }

    @NonNull
    @Override
    public Result doWork() {
        // Aquí realizamos la lógica para reducir la caducidad de los productos
        actualizarCaducidadProductos();
        return Result.success();
    }

    private void actualizarCaducidadProductos() {
        // Asumimos que los productos están en la colección Firestore "<email> Productos"
        SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences("correo", Context.MODE_PRIVATE);
        String email = sharedPrefs.getString("email", null);

        if (email == null) {
            return; // Si no tenemos email, no podemos acceder a los productos
        }

        db.collection(email + " Productos").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (DocumentSnapshot document : task.getResult().getDocuments()) {
                    Long caducidad = document.getLong("Caducidad"); // Obtener el valor como Long (Firestore almacena enteros largos)
                    if (caducidad != null) {
                        // Reducir la caducidad en 1, asegurando que no sea menor que 0
                        int nuevaCaducidad = Math.max(0, caducidad.intValue() - 1);

                        // Actualizar el documento en Firestore con el valor como entero
                        Map<String, Object> updatedData = new HashMap<>();
                        updatedData.put("Caducidad", nuevaCaducidad);

                        db.collection(email + " Productos").document(document.getId())
                                .update(updatedData)
                                .addOnSuccessListener(aVoid -> {
                                    // Éxito al actualizar
                                })
                                .addOnFailureListener(e -> {
                                    // Error al actualizar
                                });
                    }
                }
            }
        });
    }
}