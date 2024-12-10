package com.example.recipies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import LISTAS.IngredientData;
import LISTAS.StepData;
import adaptadores.adaptadorIngredientesMostrar;
import adaptadores.adaptadorPasosMostrar;

public class MostrarReceta extends AppCompatActivity {

    private FirebaseFirestore db;
    private String id;
    private String imagenUrl;
    private TextView tituloTextView;
    private TextView numPersonasTextView;
    private TextView vegetarianaTextView;
    private TextView horarioTextView;
    private TextView caloriasTextView;
    private ImageView imagenView;
    private String email;
    private RecyclerView ingredientesRecyclerView;
    private RecyclerView pasosRecyclerView;
    private adaptadorIngredientesMostrar adaptadorIngredientes;
    private adaptadorPasosMostrar adaptadorPasos;
    private List<IngredientData> ingredientesDataList = new ArrayList<>();
    private Map<String, Integer> productosDisponibles = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mostrar_receta);

        db = FirebaseFirestore.getInstance();
        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        imagenUrl = intent.getStringExtra("imagenUrl");

        SharedPreferences sharedPrefs = getSharedPreferences("correo", Context.MODE_PRIVATE);
        email = sharedPrefs.getString("email", null);

        tituloTextView = findViewById(R.id.mostrartitulo);
        numPersonasTextView = findViewById(R.id.mostrarpersonas);
        vegetarianaTextView = findViewById(R.id.mostrarvege);
        horarioTextView = findViewById(R.id.mostarhorario);
        caloriasTextView = findViewById(R.id.mostarcalorias);
        imagenView = findViewById(R.id.mostrarimagen);

        ingredientesRecyclerView = findViewById(R.id.mostraringredientes);
        pasosRecyclerView = findViewById(R.id.mostrarpasos);

        adaptadorIngredientes = new adaptadorIngredientesMostrar(this, ingredientesDataList);
        adaptadorPasos = new adaptadorPasosMostrar(this, new ArrayList<>());

        ingredientesRecyclerView.setAdapter(adaptadorIngredientes);
        ingredientesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        pasosRecyclerView.setAdapter(adaptadorPasos);
        pasosRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        cargarProductosDisponibles();
        cargarDatosReceta();
    }

    private void cargarProductosDisponibles() {
        db.collection(email + " Productos")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String nombreProducto = document.getString("Nombre");
                        int cantidad = Integer.parseInt(document.getString("Cantidad"));
                        String nombreNormalizado = adaptadorIngredientesMostrar.normalizarTexto(nombreProducto);
                        productosDisponibles.put(nombreNormalizado, cantidad);

                        Log.d("MostrarReceta", "Producto disponible: " + nombreNormalizado + " - Cantidad: " + cantidad);
                    }
                    adaptadorIngredientes.actualizarProductosDisponibles(productosDisponibles);
                })
                .addOnFailureListener(e -> Log.e("MostrarReceta", "Error al cargar productos", e));
    }

    private void cargarDatosReceta() {
        db.collection("recetas").document(id).get().addOnSuccessListener(documentSnapshot -> {
            tituloTextView.setText(documentSnapshot.getString("titulo"));
            numPersonasTextView.setText(documentSnapshot.getString("numero_Personas"));
            horarioTextView.setText(documentSnapshot.getString("horario"));
            caloriasTextView.setText(documentSnapshot.getString("calorias") + " Kcal");

            if ("Si".equals(documentSnapshot.getString("vegetariana"))) {
                vegetarianaTextView.setText("Vegetariana");
            } else {
                vegetarianaTextView.setVisibility(View.GONE);
            }

            Picasso.get().load(imagenUrl).into(imagenView);

            List<String> ingredientesList = (List<String>) documentSnapshot.get("ingredientes");
            if (ingredientesList != null) {
                for (String nombreIngrediente : ingredientesList) {
                    ingredientesDataList.add(new IngredientData(nombreIngrediente));
                }
                adaptadorIngredientes.notifyDataSetChanged();
            }

            List<String> pasosList = (List<String>) documentSnapshot.get("pasos");
            if (pasosList != null) {
                for (String paso : pasosList) {
                    adaptadorPasos.addStep(new StepData(paso));
                }
                adaptadorPasos.notifyDataSetChanged();
            }
        });
    }
}