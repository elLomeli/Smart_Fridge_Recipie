package com.example.recipies;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import POJO.F_V;
import POJO.Productos;
import POJO.RecetasPlan;
import adaptadores.AdaptadorLista;
import adaptadores.AdaptadorListaFV;

public class agregaProducto  extends DialogFragment {

    private RecyclerView recyclerView;
    private AdaptadorLista adapter;
    private FirebaseFirestore db;
    private ArrayList<Productos> productos;
    private SearchView searchView;


    public agregaProducto() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_agrega_producto, container, false);
        db = FirebaseFirestore.getInstance();
        productos = new ArrayList<>();
        recyclerView = v.findViewById(R.id.recyclerViewlista);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
        recyclerView.setHasFixedSize(true);

        adapter = new AdaptadorLista(requireContext(), productos);
        recyclerView.setAdapter(adapter);

        searchView = v.findViewById(R.id.searchView);


        // Eventos para el SearchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterFirestoreDataTitulo(newText);
                return true;
            }
        });

        Evento();
        return v;
    }

    private void filterFirestoreDataTitulo(String query) {
        ArrayList<Productos> info = new ArrayList<>();
        for (Productos obj : productos) {
            if (obj.getNombre().toLowerCase().contains(query.toLowerCase())) {
                info.add(obj);
            }
        }
        AdaptadorLista filteredAdapter = new AdaptadorLista(getContext(), info);
        recyclerView.setAdapter(filteredAdapter);
    }

    private void Evento() {
        // Aquí se realiza la consulta a Firestore para obtener los productos
        db.collection("productos")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Obtener los datos de cada documento y agregarlos a la lista de productos
                                Productos producto = document.toObject(Productos.class);
                                productos.add(producto);
                            }
                            // Notificar al adaptador que los datos han cambiado
                            adapter.notifyDataSetChanged();
                        } else {
                            //error al buscar
                        }
                    }
                });
    }
}