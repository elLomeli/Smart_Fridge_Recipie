package com.example.recipies;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import DeletePhotos.ImageCheckerRecepies;
import POJO.Recetas;
import adaptadores.adaptadorRecetas;


public class Crud_Recipies_Fragment extends Fragment {
    private FloatingActionButton agregar;
    private FloatingActionButton ejecutarCabezas;
    private FloatingActionButton verdugoDeCabezas;
    private FirebaseFirestore db;
    private RecyclerView mRecycler;
    private adaptadorRecetas mAdapter;
    private Query query;
    private SearchView buscador;
    private ImageCheckerRecepies imageChecker;

    public Crud_Recipies_Fragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_crud__recipies_, container, false);
        agregar = v.findViewById(R.id.agregarRecetas);
        buscador = v.findViewById(R.id.searchRecetas);
        ejecutarCabezas = v.findViewById(R.id.eliminarRecetas);
        verdugoDeCabezas = v.findViewById(R.id.verdugoRecetas);
        mRecycler = v.findViewById(R.id.recyclerviewRecetas);
        imageChecker = new ImageCheckerRecepies();
        db = FirebaseFirestore.getInstance();


        agregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Recipies_Fragment fragment = new Recipies_Fragment();
                fragment.show(getParentFragmentManager(),"Agregar Recetas");
                buscador.clearFocus();
            }
        });

        buscador.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String t) {
                buscador(capitalize(t));
                return false;
            }

            @Override
            public boolean onQueryTextChange(String nextt) {
                try {
                    buscador(capitalize(nextt));
                } catch (Exception e) {
                    Log.e(TAG, "Error al buscar Recetas", e);
                }
                return false;
            }
        });

        ejecutarCabezas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapter.mostrarCheckBoxCabezas();
            }
        });
        imageChecker.start();
        actualizarPantalla();
        return v;
    }

    private void actualizarPantalla() {
        mRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        query = db.collection("recetas");
        FirestoreRecyclerOptions<Recetas> firestoreRecyclerOptions = new FirestoreRecyclerOptions.Builder<Recetas>().setQuery(query, Recetas.class).build();
        mAdapter = new adaptadorRecetas(firestoreRecyclerOptions, getParentFragmentManager(), ejecutarCabezas, verdugoDeCabezas, getContext());
        mRecycler.setAdapter(mAdapter);
    }
    private void mostrarTodosProductos() {
        if (mAdapter != null) {
            mAdapter.stopListening(); // Detener la escucha del adaptador actual
        }
        FirestoreRecyclerOptions<Recetas> firestoreRecyclerOptions = new FirestoreRecyclerOptions.Builder<Recetas>().setQuery(query, Recetas.class).build();
        mAdapter = new adaptadorRecetas(firestoreRecyclerOptions, getParentFragmentManager(),ejecutarCabezas,verdugoDeCabezas,getContext());
        mAdapter.startListening();
        mRecycler.setAdapter(mAdapter);
    }

    private void buscador(String t) {
        if (t.isEmpty()) {
            mostrarTodosProductos();
            return;
        }
        mAdapter.stopListening(); // Detener la escucha del adaptador actual

        Query searchQuery = query.orderBy("titulo").startAt(t).endAt(t + "~");
        searchQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        FirestoreRecyclerOptions<Recetas> firestoreRecyclerOptions = new FirestoreRecyclerOptions.Builder<Recetas>().setQuery(searchQuery, Recetas.class).build();
                        mAdapter = new adaptadorRecetas(firestoreRecyclerOptions, getParentFragmentManager(),ejecutarCabezas,verdugoDeCabezas,getContext());
                        mAdapter.startListening();
                        mRecycler.setAdapter(mAdapter);
                    } else {
                        mAdapter.stopListening(); // Detener la escucha del adaptador actual
                        mostrarTodosProductos();
                    }
                } else {
                    Log.e(TAG, "Error al buscar productos", task.getException());
                }
            }
        });
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return s.trim().substring(0, 1).toUpperCase() + s.trim().substring(1).toLowerCase();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAdapter.startListening();
        mRecycler.setAdapter(mAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }
}