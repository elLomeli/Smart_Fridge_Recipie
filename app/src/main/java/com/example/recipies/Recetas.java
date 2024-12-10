package com.example.recipies;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import POJO.RecetasUsuario;
import adaptadores.adaptadorRecetasUsuarios;
public class Recetas extends Fragment {
    private RecyclerView recyclerView;
    private ArrayList<RecetasUsuario> recetasUsuarios;
    private adaptadorRecetasUsuarios Adaptador;
    private FirebaseFirestore db;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SearchView searchView;
    private Spinner extras;
    private ArrayAdapter<CharSequence> EX;
    private Spinner secondarySpinner;
    private String textoSeleccionado;
    private boolean mostrarSoloFavoritos = false;

    public Recetas() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_recetas, container, false);

        swipeRefreshLayout = v.findViewById(R.id.swipeRefreshLayout); // Inicializar SwipeRefreshLayout
        recyclerView = v.findViewById(R.id.recyclerviewRecetasUsuarios);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        db = FirebaseFirestore.getInstance();
        recetasUsuarios = new ArrayList<>();
        Adaptador = new adaptadorRecetasUsuarios(getContext(), recetasUsuarios, this::actualizarFavoritosYRating);
        recyclerView.setAdapter(Adaptador);

        searchView = v.findViewById(R.id.searchView);
        extras = v.findViewById(R.id.extrasSpinner);
        secondarySpinner = v.findViewById(R.id.secondarySpinner);

        EX = ArrayAdapter.createFromResource(getContext(), R.array.Ex, android.R.layout.simple_spinner_item);
        EX.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        extras.setAdapter(EX);
        extras.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                textoSeleccionado = parentView.getItemAtPosition(position).toString();
                mostrarSoloFavoritos = "Favoritos".equals(textoSeleccionado);

                if ("Todos".equals(textoSeleccionado)) {
                    mostrarRecetasTodas();
                } else {
                    configurarBusquedaOSpinner();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });

        configurarBusquedaOSpinner();

        // Configurar el SwipeRefreshLayout para recargar datos
        swipeRefreshLayout.setOnRefreshListener(() -> {
            obtenerRecetas(); // Llamar al método para cargar las recetas
        });

        return v;
    }

    private void configurarBusquedaOSpinner() {
        if ("Numero de Personas".equals(textoSeleccionado)) {
            searchView.setVisibility(View.VISIBLE);
            secondarySpinner.setVisibility(View.GONE);
            searchView.setInputType(InputType.TYPE_CLASS_NUMBER);
            searchView.setQueryHint("Buscar por número de personas");
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) { return false; }
                @Override
                public boolean onQueryTextChange(String newText) {
                    filtrarDatosPorNumeroPersonas(newText);
                    return true;
                }
            });
        } else if ("Horario".equals(textoSeleccionado)) {
            searchView.setVisibility(View.GONE);
            configurarSpinnerSecundario(R.array.horarios_array, this::filtrarDatosPorHorario);
        } else if ("Vegetariana".equals(textoSeleccionado)) {
            searchView.setVisibility(View.GONE);
            configurarSpinnerSecundario(R.array.vegetariana_array, this::filtrarDatosPorVegetariana);
        } else if ("Favoritos".equals(textoSeleccionado)) {
            mostrarRecetasFavoritas();
        } else {
            searchView.setVisibility(View.VISIBLE);
            secondarySpinner.setVisibility(View.GONE);
            searchView.setInputType(InputType.TYPE_CLASS_TEXT);
            searchView.setQueryHint("Buscar por título");
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) { return false; }
                @Override
                public boolean onQueryTextChange(String newText) {
                    filtrarDatosPorTitulo(newText);
                    return true;
                }
            });
        }
    }

    private void configurarSpinnerSecundario(int arrayResId, FiltroSpinnerCallback callback) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), arrayResId, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        secondarySpinner.setAdapter(adapter);
        secondarySpinner.setVisibility(View.VISIBLE);

        secondarySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedOption = parent.getItemAtPosition(position).toString();
                if ("Todos".equals(selectedOption)) {
                    mostrarRecetasTodas();
                } else {
                    callback.onFilter(selectedOption);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void mostrarRecetasFavoritas() {
        ArrayList<RecetasUsuario> favoritas = new ArrayList<>();
        for (RecetasUsuario receta : recetasUsuarios) {
            if (Adaptador.esFavorito(receta.getId())) {
                favoritas.add(receta);
            }
        }
        Adaptador.actualizarLista(favoritas);
    }

    private void mostrarRecetasTodas() {
        Adaptador.actualizarLista(recetasUsuarios);
    }

    private void actualizarFavoritosYRating() {
        if (mostrarSoloFavoritos) {
            mostrarRecetasFavoritas();
        } else {
            Adaptador.notifyDataSetChanged();
        }
    }

    private void filtrarDatosPorTitulo(String query) {
        ArrayList<RecetasUsuario> filtradas = new ArrayList<>();
        for (RecetasUsuario receta : recetasUsuarios) {
            if (receta.getTitulo().toLowerCase().contains(query.toLowerCase())) {
                filtradas.add(receta);
            }
        }
        Adaptador.actualizarLista(filtradas);
    }

    private void filtrarDatosPorNumeroPersonas(String query) {
        ArrayList<RecetasUsuario> filtradas = new ArrayList<>();
        for (RecetasUsuario receta : recetasUsuarios) {
            if (String.valueOf(receta.getNumero_Personas()).contains(query)) {
                filtradas.add(receta);
            }
        }
        Adaptador.actualizarLista(filtradas);
    }

    private void filtrarDatosPorHorario(String opcionSeleccionada) {
        ArrayList<RecetasUsuario> filtradas = new ArrayList<>();
        for (RecetasUsuario receta : recetasUsuarios) {
            if (receta.getHorario().equalsIgnoreCase(opcionSeleccionada)) {
                filtradas.add(receta);
            }
        }
        Adaptador.actualizarLista(filtradas);
    }

    private void filtrarDatosPorVegetariana(String opcionSeleccionada) {
        ArrayList<RecetasUsuario> filtradas = new ArrayList<>();
        for (RecetasUsuario receta : recetasUsuarios) {
            if (String.valueOf(receta.getVegetariana()).equalsIgnoreCase(opcionSeleccionada)) {
                filtradas.add(receta);
            }
        }
        Adaptador.actualizarLista(filtradas);
    }

    @Override
    public void onStart() {
        super.onStart();
        obtenerRecetas();
    }

    private void obtenerRecetas() {
        db.collection("recetas").orderBy("titulo", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e("Error Firebase", error.getMessage());
                            return;
                        }

                        recetasUsuarios.clear();
                        for (DocumentChange dc : value.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                RecetasUsuario receta = dc.getDocument().toObject(RecetasUsuario.class);
                                receta.setId(dc.getDocument().getId());
                                recetasUsuarios.add(receta);
                            }
                        }
                        Adaptador.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false); // Detener el ícono de actualización
                    }
                });
    }

    interface FiltroSpinnerCallback {
        void onFilter(String opcionSeleccionada);
    }
}