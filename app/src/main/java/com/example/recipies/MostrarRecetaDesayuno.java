package com.example.recipies;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import LISTAS.IngredientData;
import LISTAS.StepData;
import adaptadores.adaptadorIngredientesMostrar;
import adaptadores.adaptadorPasosMostrar;

public class MostrarRecetaDesayuno extends AppCompatActivity {
    private FirebaseFirestore db;
    private String id,perfil;
    private Boolean empezar;
    private String imagenUrl;
    private TextView tituloTextView;
    private TextView numPersonasTextView;
    private TextView vegetarianaTextView;
    private TextView horarioTextView;
    private TextView caloriasTextView;
    private ImageView imagenView;
    private RecyclerView ingredientesRecyclerView;
    private RecyclerView pasosRecyclerView;
    private adaptadorIngredientesMostrar adaptadorIngredientes;
    private adaptadorPasosMostrar adaptadorPasos;
    private List<IngredientData> ingredientesDataList = new ArrayList<>();
    private List<StepData> pasosDataList = new ArrayList<>();
    private Button Btnempezar,Btnterminar;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mostrar_receta);
        db = FirebaseFirestore.getInstance();
        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        perfil = intent.getStringExtra("perfil");
        empezar = intent.getBooleanExtra("empezar",false);
        imagenUrl = intent.getStringExtra("imagenUrl");
        tituloTextView = findViewById(R.id.mostrartitulo);
        numPersonasTextView = findViewById(R.id.mostrarpersonas);
        vegetarianaTextView = findViewById(R.id.mostrarvege);
        horarioTextView = findViewById(R.id.mostarhorario);
        caloriasTextView = findViewById(R.id.mostarcalorias);
        imagenView = findViewById(R.id.mostrarimagen);
        Btnempezar = findViewById(R.id.botonempezar);
        Btnterminar = findViewById(R.id.botonterminar);

        SharedPreferences sharedPrefs = getSharedPreferences("correo", Context.MODE_PRIVATE);
        email = sharedPrefs.getString("email",null);

        // Inicializar RecyclerViews y adaptadores
        ingredientesRecyclerView = findViewById(R.id.mostraringredientes);
        pasosRecyclerView = findViewById(R.id.mostrarpasos);
        pasosRecyclerView.setVisibility(View.GONE);
        adaptadorIngredientes = new adaptadorIngredientesMostrar(MostrarRecetaDesayuno.this, ingredientesDataList);
        adaptadorPasos = new adaptadorPasosMostrar(MostrarRecetaDesayuno.this, pasosDataList);

        // Configura RecyclerView para Ingredientes
        ingredientesRecyclerView.setAdapter(adaptadorIngredientes);
        ingredientesRecyclerView.setLayoutManager(new LinearLayoutManager(MostrarRecetaDesayuno.this));

        // Configura RecyclerView para Pasos
        pasosRecyclerView.setAdapter(adaptadorPasos);
        pasosRecyclerView.setLayoutManager(new LinearLayoutManager(MostrarRecetaDesayuno.this));


        db.collection("recetas").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                tituloTextView.setText(documentSnapshot.getString("titulo"));
                numPersonasTextView.setText(documentSnapshot.getString("numero_Personas"));

                if ("Si".equals(documentSnapshot.getString("vegetariana"))) {
                    vegetarianaTextView.setText("Vegetariana");
                } else {
                    vegetarianaTextView.setVisibility(View.GONE);
                }
                horarioTextView.setText(documentSnapshot.getString("horario"));
                caloriasTextView.setText(documentSnapshot.getString("calorias"));
                Picasso.get().load(imagenUrl).into(imagenView);

                // Obtener la lista de ingredientes almacenados en Firestore
                List<String> ingredientesList = (List<String>) documentSnapshot.get("ingredientes");
                if (ingredientesList != null) {
                    for (String nombreIngrediente : ingredientesList) {
                        // Crear una instancia de IngredientData y establecer el nombre del ingrediente
                        IngredientData ingredienteData = new IngredientData(nombreIngrediente);
                        // Agregar el ingrediente al adaptador de ingredientes
                        adaptadorIngredientes.addIngredient(ingredienteData);
                    }
                    // Notificar al adaptador que los datos han cambiado
                    adaptadorIngredientes.notifyDataSetChanged();
                }

                // Obtener la lista de pasos almacenados en Firestore
                List<String> pasosList = (List<String>) documentSnapshot.get("pasos");
                if (pasosList != null) {
                    for (String nombrePaso : pasosList) {
                        // Crear una instancia de StepData y establecer el nombre del paso
                        StepData pasoData = new StepData(nombrePaso);
                        // Agregar el paso al adaptador de pasos
                        adaptadorPasos.addStep(pasoData);
                    }
                    // Notificar al adaptador que los datos han cambiado
                    adaptadorPasos.notifyDataSetChanged();
                }
            }
        });

        Btnempezar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Crear un cuadro de diálogo de confirmación
                AlertDialog.Builder builder = new AlertDialog.Builder(MostrarRecetaDesayuno.this);
                builder.setMessage("¿Está segur@ de querer empezar esta receta?")
                        .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Btnempezar.setVisibility(View.GONE);
                                Btnterminar.setVisibility(View.VISIBLE);
                                pasosRecyclerView.setVisibility(View.VISIBLE);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // El usuario ha cancelado, no hacer nada
                                dialog.dismiss();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });


        Btnterminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MostrarRecetaDesayuno.this);
                builder.setMessage("¿Está seguro de querer terminar esta receta?")
                        .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int i) {
                                Btnempezar.setVisibility(View.GONE);
                                Btnterminar.setVisibility(View.GONE);
                                pasosRecyclerView.setVisibility(View.GONE);
                                float caloriasDesayuno = Float.parseFloat(caloriasTextView.getText().toString());

                                // Guardar el ID de la receta en SharedPreferences
                                SharedPreferences pref = getSharedPreferences("RecetasPreparadas", MODE_PRIVATE);
                                SharedPreferences.Editor edit = pref.edit();
                                edit.putString("recetaPreparadaDesayuno" + perfil, id); // Guarda el ID de la receta
                                edit.apply();

                                // Guardar valor
                                SharedPreferences prefs = getSharedPreferences("CaloriasRestantes" + perfil, MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putFloat("caloriasDesayuno", caloriasDesayuno);
                                editor.apply();

                                // Marcar que se ha hecho clic en "Empezar" hoy
                                markAsStartedToday();

                                // Obtiene la fecha y hora actual
                                Calendar calendar = Calendar.getInstance();
                                Date currentDate = calendar.getTime();

                                // Formatea la fecha como desees
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                                String formattedDate = dateFormat.format(currentDate);

                                // Formatea la hora como desees
                                SimpleDateFormat horaFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                                String formattedHora = horaFormat.format(currentDate);

                                // Construye la referencia al documento
                                DocumentReference documentReference = db.collection(email).document("perfilProgreso " + perfil + " " + formattedDate);

                                // Verifica si el documento ya existe
                                documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot document = task.getResult();
                                            if (document.exists()) {
                                                // El documento ya existe, actualiza los datos
                                                Map<String, Object> data = new HashMap<>();
                                                data.put("desayunoCalorias", caloriasDesayuno);
                                                data.put("desayunoHora", formattedHora);

                                                documentReference.update(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        // Datos actualizados exitosamente
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(MostrarRecetaDesayuno.this, "Progreso No guardado", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            } else {
                                                // El documento no existe, créalo
                                                Map<String, Object> newData = new HashMap<>();
                                                newData.put("desayunoCalorias", caloriasDesayuno);
                                                newData.put("desayunoHora", formattedHora);

                                                documentReference.set(newData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        // Documento creado exitosamente
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(MostrarRecetaDesayuno.this, "Progreso No guardado", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        } else {
                                            // Maneja el error al verificar la existencia del documento
                                            Toast.makeText(MostrarRecetaDesayuno.this, "Error al verificar existencia del documento", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int i) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }


    private void checkAndShowStartButton() {
        SharedPreferences prefs = getSharedPreferences("EmpezarRecetaDesayuno" + perfil, MODE_PRIVATE);
        boolean startedToday = prefs.getBoolean("startedToday", false);

        // Verificar si ya se ha hecho clic en "Empezar" hoy y mostrar el botón si es posible
        if (!startedToday) {
            Btnempezar.setVisibility(View.VISIBLE);
        }
        else {
            Btnempezar.setVisibility(View.GONE);
        }
    }

    private void markAsStartedToday() {
        SharedPreferences prefs = getSharedPreferences("EmpezarRecetaDesayuno" + perfil, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("startedToday", true);
        editor.apply();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Verificar si ya se ha hecho clic en "Empezar" hoy y mostrar el botón si es posible
        checkAndShowStartButton();
    }
}