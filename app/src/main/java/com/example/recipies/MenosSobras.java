package com.example.recipies;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import POJO.RecetasUsuario;
import adaptadores.adaptadorRecetasUsuarios;
public class MenosSobras extends Fragment {

    private RecyclerView recyclerView;
    private adaptadorRecetasUsuarios Adaptador;
    private FirebaseFirestore db;
    private String email;
    private List<String> productosCercaDeCaducar = new ArrayList<>();
    private ArrayList<RecetasUsuario> recetasUsuarios = new ArrayList<>();
    private ProgressDialog progressDialog;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SharedPreferences ratingPrefs;

    public MenosSobras() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        SharedPreferences sharedPrefs = requireActivity().getSharedPreferences("correo", Context.MODE_PRIVATE);
        email = sharedPrefs.getString("email", null);
        ratingPrefs = requireActivity().getSharedPreferences("Calificaciones", Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_menos_sobras, container, false);
        recyclerView = view.findViewById(R.id.recyclerviewRecetasUsuarios);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        Adaptador = new adaptadorRecetasUsuarios(getContext(), recetasUsuarios, this::actualizarFavoritosYCalificacion);
        recyclerView.setAdapter(Adaptador);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Buscando recetas...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        swipeRefreshLayout.setOnRefreshListener(this::obtenerProductosCercaDeCaducar);

        obtenerProductosCercaDeCaducar();
        return view;
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
                            Log.d("MenosSobras", "Producto cercano a caducar: " + nombreProducto);
                        }
                    }
                    obtenerRecetas();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.e("MenosSobras", "Error al obtener productos cerca de caducar", e);
                    Toast.makeText(getContext(), "Error al cargar productos", Toast.LENGTH_SHORT).show();
                });
    }

    private void obtenerRecetas() {
        db.collection("recetas")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    recetasUsuarios.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        RecetasUsuario receta = document.toObject(RecetasUsuario.class);
                        if (receta != null && contieneIngredientesCercaDeCaducar(receta.getIngredientes())) {
                            receta.setId(document.getId());
                            recetasUsuarios.add(receta);
                            Log.d("MenosSobras", "Receta añadida: " + receta.getTitulo());
                        }
                    }

                    ordenarRecetas();

                    Adaptador.notifyDataSetChanged();
                    progressDialog.dismiss();

                    if (recetasUsuarios.isEmpty()) {
                        mostrarDialogoNoRecetasEncontradas();
                    } else {
                        Toast.makeText(getContext(), "Recetas encontradas", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.e("MenosSobras", "Error al obtener recetas", e);
                    Toast.makeText(getContext(), "Error al cargar recetas", Toast.LENGTH_SHORT).show();
                });
    }

    private void mostrarDialogoNoRecetasEncontradas() {
        new AlertDialog.Builder(getContext())
                .setTitle("Sin recetas")
                .setMessage("No se encontraron recetas con los productos cercanos a caducar o no cuenta con productos")
                .setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void ordenarRecetas() {
        Collections.sort(recetasUsuarios, (receta1, receta2) -> {
            float calificacion1 = obtenerCalificacionDePreferencias(receta1.getId());
            float calificacion2 = obtenerCalificacionDePreferencias(receta2.getId());

            int calificacionComparison = Float.compare(calificacion2, calificacion1);
            if (calificacionComparison != 0) {
                return calificacionComparison;
            }
            return receta1.getTitulo().compareToIgnoreCase(receta2.getTitulo());
        });
    }

    private float obtenerCalificacionDePreferencias(String id) {
        return ratingPrefs.getFloat(id, 0.0f);
    }

    private boolean contieneIngredientesCercaDeCaducar(List<String> ingredientes) {
        LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
        int umbralSimilitud = 1;

        for (String ingrediente : ingredientes) {
            String ingredienteNormalizado = normalizarTexto(ingrediente);
            Log.d("MenosSobras", "Ingrediente normalizado: " + ingredienteNormalizado);

            for (String productoCaducando : productosCercaDeCaducar) {
                String productoNormalizado = normalizarTexto(productoCaducando);
                Log.d("MenosSobras", "Producto normalizado: " + productoNormalizado);

                if (ingredienteNormalizado.equals(productoNormalizado) ||
                        ingredienteNormalizado.contains(productoNormalizado) ||
                        productoNormalizado.contains(ingredienteNormalizado)) {
                    Log.d("MenosSobras", "Coincidencia exacta o parcial encontrada: " + ingredienteNormalizado + " - " + productoNormalizado);
                    return true;
                }

                int distancia = levenshteinDistance.apply(ingredienteNormalizado, productoNormalizado);
                if (distancia <= umbralSimilitud) {
                    Log.d("MenosSobras", "Coincidencia difusa encontrada (distancia " + distancia + "): " + ingredienteNormalizado + " - " + productoNormalizado);
                    return true;
                }
            }
        }
        Log.d("MenosSobras", "No se encontraron coincidencias para los ingredientes.");
        return false;
    }

    private String normalizarTexto(String texto) {
        if (texto == null) return "";
        texto = texto.toLowerCase()
                .replaceAll("[áàäâ]", "a")
                .replaceAll("[éèëê]", "e")
                .replaceAll("[íìïî]", "i")
                .replaceAll("[óòöô]", "o")
                .replaceAll("[úùüû]", "u")
                .replaceAll("[ñ]", "n")
                .replaceAll("[^a-zA-Z\\s]", "")
                .replaceAll("\\b(chico|chicos|grande|grandes)\\b", "")
                .replaceAll("\\b(gramos?|mililitros?|litros?|cucharadita?s?|cucharadas?|piezas?|vaso|unidad(?:es)?|taza(?:s)?|pellizco?s?)\\b", "")
                .replaceAll("\\s+", " ")
                .trim();

        if (texto.endsWith("s") && texto.length() > 3) {
            texto = texto.substring(0, texto.length() - 1);
        }

        return texto;
    }

    private void actualizarFavoritosYCalificacion() {
        Adaptador.notifyDataSetChanged();
    }
}