package com.example.recipies;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import POJO.F_V;
import POJO.Productos;
import adaptadores.AdaptadorLista;
import adaptadores.AdaptadorListaFV;


public class agregaFV extends DialogFragment {

    private RecyclerView recyclerView;
    private AdaptadorListaFV adapter;
    private FirebaseFirestore db;
    private ArrayList<F_V> productos;
    private SearchView searchView;


    public agregaFV() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_agrega_f_v, container, false);

        db = FirebaseFirestore.getInstance();
        productos = new ArrayList<>();
        recyclerView = v.findViewById(R.id.recyclerViewlistaFV);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));

        adapter = new AdaptadorListaFV(requireContext(), productos);
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
        ArrayList<F_V> info = new ArrayList<>();
        for (F_V obj : productos) {
            if (obj.getNombre().toLowerCase().contains(query.toLowerCase())) {
                info.add(obj);
            }
        }
        AdaptadorListaFV filteredAdapter = new AdaptadorListaFV(getContext(), info);
        recyclerView.setAdapter(filteredAdapter);
    }

    private void Evento() {
        db.collection("Frutas y Verduras")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e("Error Firestore", "Error al obtener productos: " + error.getMessage());
                            return;
                        }

                        productos.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            F_V producto = doc.toObject(F_V.class);
                            productos.add(producto);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }


}


