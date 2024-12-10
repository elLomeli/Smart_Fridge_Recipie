package com.example.recipies;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import POJO.RecetasPlan;
import adaptadores.adaptadorRecetasPlanComida;

public class RecetasPlanAlimenticioComida extends Fragment {

    private RecyclerView recyclerView;
    private ArrayList<RecetasPlan> recetasPlanes;
    private adaptadorRecetasPlanComida Adaptador;
    private FirebaseFirestore db;
    private SearchView searchView;
    private String id,email;
    private PlanPerfilViewModel viewModel;
    private String caloriasRestantesFormateado;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            id = getArguments().getString("perfil");
        }

        viewModel = new ViewModelProvider(requireActivity()).get(PlanPerfilViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_recetas_plan_alimenticio, container, false);

        // Inicialización de vistas y adaptadores aquí
        recyclerView = view.findViewById(R.id.recyclerviewPlan);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        db = FirebaseFirestore.getInstance();
        recetasPlanes = new ArrayList<>();
        Adaptador = new adaptadorRecetasPlanComida(requireContext(), recetasPlanes, id);
        recyclerView.setAdapter(Adaptador);
        searchView = view.findViewById(R.id.searchViewPlan);
        SharedPreferences sharedPrefs = requireActivity().getSharedPreferences("correo", Context.MODE_PRIVATE);
        email = sharedPrefs.getString("email", null);




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
        return view;
    }
    private void filterFirestoreDataTitulo(String query) {
        ArrayList<RecetasPlan> info = new ArrayList<>();
        for (RecetasPlan obj : recetasPlanes) {
            if (obj.getTitulo().toLowerCase().contains(query.toLowerCase())) {
                info.add(obj);
            }
        }
        adaptadorRecetasPlanComida Adapter = new adaptadorRecetasPlanComida(getContext(), info,id);
        recyclerView.setAdapter(Adapter);
    }

    private void Evento() {

        SharedPreferences prefsUs = requireActivity().getSharedPreferences("CaloriasUsuario" + id,Context.MODE_PRIVATE);
        float calorias = prefsUs.getFloat("calorias", 0.0f);

        int porcentajeMaximo = (int) (calorias * 0.40);

        db.collection("recetas")
                .whereEqualTo("numero_Personas", "1")
                .whereEqualTo("horario", "Comida")
                .whereEqualTo("plan","Si")
                .orderBy("calorias", Query.Direction.ASCENDING) // Ordena por calorias
                .whereLessThanOrEqualTo("calorias", String.valueOf(porcentajeMaximo)) // Aplica el filtro de desigualdad
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e("Error Firebase", error.getMessage());
                            return;
                        }

                        recetasPlanes.clear();
                        for (DocumentChange dc : value.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                RecetasPlan recetasplan = dc.getDocument().toObject(RecetasPlan.class);
                                recetasplan.id = dc.getDocument().getId(); // Asignar el ID único
                                recetasPlanes.add(recetasplan);
                            }
                        }
                        Adaptador.notifyDataSetChanged();
                    }
                });
    }

    public static RecetasPlanAlimenticioComida newInstance(String id) {
        RecetasPlanAlimenticioComida fragment = new RecetasPlanAlimenticioComida();
        Bundle args = new Bundle();
        args.putString("perfil", id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.getCaloriasRestantesLiveData().observe(getViewLifecycleOwner(), caloriasRestantes -> {
           caloriasRestantesFormateado = String.format("%.0f", caloriasRestantes);
        });
    }
}