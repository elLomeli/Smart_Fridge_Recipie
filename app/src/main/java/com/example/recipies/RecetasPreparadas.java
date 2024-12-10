package com.example.recipies;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import POJO.RecetasPlan;
import adaptadores.adaptadorRecetasPlanCena;
import adaptadores.adaptadorRecetasPlanPreparados;

public class RecetasPreparadas extends Fragment {

    private String perfil;
    private String email;
    private FirebaseFirestore db;
    private RecyclerView recetasRecyclerView;
    private adaptadorRecetasPlanPreparados adaptador;
    private ArrayList<RecetasPlan> recetasPlanArrayList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            perfil = getArguments().getString("perfil");
        }
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recetas_preparadas, container, false);

        recetasRecyclerView = view.findViewById(R.id.recyclerviewPlan);
        adaptador = new adaptadorRecetasPlanPreparados(getContext(), recetasPlanArrayList, perfil);
        recetasRecyclerView.setAdapter(adaptador);
        recetasRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        SharedPreferences sharedPrefs = getActivity().getSharedPreferences("correo", Context.MODE_PRIVATE);
        email = sharedPrefs.getString("email", null);

        SharedPreferences prefs = getActivity().getSharedPreferences("RecetasPreparadas", Context.MODE_PRIVATE);
        String desayunoId = prefs.getString("recetaPreparadaDesayuno" + perfil, null);
        String comidaId = prefs.getString("recetaPreparadaComida" + perfil, null);
        String cenaId = prefs.getString("recetaPreparadaCena" + perfil, null);

        // Cargar recetas preparadas para desayuno, comida y cena si existen
        if (desayunoId != null) {
            cargarReceta(desayunoId);
        }
        if (comidaId != null) {
            cargarReceta(comidaId);
        }
        if (cenaId != null) {
            cargarReceta(cenaId);
        }

        return view;
    }

    private void cargarReceta(String recetaId) {
        db.collection("recetas").document(recetaId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    // Extrae el título e imagen de la receta
                    String titulo = documentSnapshot.getString("titulo");
                    String imagenUrl = documentSnapshot.getString("imagenUrl");

                    // Crea una instancia de RecetasPlan con solo el título y la imagen
                    RecetasPlan receta = new RecetasPlan();
                    receta.setTitulo(titulo);
                    receta.setImagenUrl(imagenUrl);
                    receta.setId(recetaId); // Guarda el ID para posibles usos futuros

                    // Agrega la receta a la lista
                    recetasPlanArrayList.add(receta);
                    adaptador.notifyDataSetChanged();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "Error al cargar la receta", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static RecetasPreparadas newInstance(String perfil) {
        RecetasPreparadas fragment = new RecetasPreparadas();
        Bundle args = new Bundle();
        args.putString("perfil", perfil);
        fragment.setArguments(args);
        return fragment;
    }
}